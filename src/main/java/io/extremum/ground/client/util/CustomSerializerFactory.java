package io.extremum.ground.client.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

/**
 * Измененный {@link BeanSerializerFactory} для подстановки {@link CustomEnumSerializer} в {@link #buildEnumSerializer}
 */
public class CustomSerializerFactory extends BeanSerializerFactory {

    @Override
    public SerializerFactory withConfig(SerializerFactoryConfig config) {
        if (_factoryConfig == config) {
            return this;
        }
        return new CustomSerializerFactory(config);
    }

    protected CustomSerializerFactory(SerializerFactoryConfig config)
    {
        super(config);
    }

    public final static BeanSerializerFactory instance = new CustomSerializerFactory(null);

    protected JsonSerializer<?> buildEnumSerializer(SerializationConfig config,
                                                    JavaType type, BeanDescription beanDesc)
            throws JsonMappingException
    {
        /* As per [databind#24], may want to use alternate shape, serialize as JSON Object.
         * Challenge here is that EnumSerializer does not know how to produce
         * POJO style serialization, so we must handle that special case separately;
         * otherwise pass it to EnumSerializer.
         */
        JsonFormat.Value format = beanDesc.findExpectedFormat(null);
        if (format.getShape() == JsonFormat.Shape.OBJECT) {
            // one special case: suppress serialization of "getDeclaringClass()"...
            ((BasicBeanDescription) beanDesc).removeProperty("declaringClass");
            // returning null will mean that eventually BeanSerializer gets constructed
            return null;
        }
        @SuppressWarnings("unchecked")
        Class<Enum<?>> enumClass = (Class<Enum<?>>) type.getRawClass();
        JsonSerializer<?> ser = CustomEnumSerializer.construct(enumClass, config, beanDesc, format);
        // [databind#120]: Allow post-processing
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                ser = mod.modifyEnumSerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }
}
