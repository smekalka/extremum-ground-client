package io.extremum.ground.client.builder.core.outputfield

import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.sharedmodels.basic.GraphQlList
import io.extremum.sharedmodels.basic.StringOrMultilingual
import io.extremum.sharedmodels.basic.StringOrObject
import io.extremum.sharedmodels.descriptor.Descriptor
import io.extremum.sharedmodels.structs.IntegerRangeOrValue
import org.springframework.util.ClassUtils
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

object OutputFieldUtils {

    /**
     * Все поля указанного класса с иерархией вниз
     */
    fun allFields(clazz: KClass<*>): List<OutputFieldStr> = allFields(clazz.java).map { it.toOutputFieldStr() }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> allFields(clazz: Class<T>): List<OutputFieldKPr<T, out Any?>> {
        val properties = allProperties(clazz.kotlin.java)
        return properties.mapNotNull { property ->
            val fieldType = property.returnType.jvmErasure
            if (fieldType.isSubclassOf(Collection::class) || fieldType == GraphQlList::class) {
                // Для выборок по вложенным спискам нужны отдельные запросы с id основной сущности и paging
                null
            } else if (fieldType.isSimple()) {
                field(property)
            } else if (clazz == fieldType.java) {
                null
            } else {
                val nested = allFields(fieldType.java) as List<OutputFieldKPr<Any, Any>>
                field(property, nested)
            }
        }
    }

    private fun <T : Any> allProperties(clazz: Class<T>): Collection<KProperty1<T, *>> {
        val fields = clazz.kotlin.declaredMemberProperties
        val superclass = clazz.superclass
        return if (superclass == null) fields else fields + allProperties(superclass) as Collection<KProperty1<T, *>>
    }

    private fun KClass<*>.isSimple(): Boolean =
        ClassUtils.isPrimitiveOrWrapper(this.java) ||
                this in SIMPLE_TYPES ||
                this.isSubclassOf(Enum::class)

    private val SIMPLE_TYPES = listOf(
        String::class,
        Int::class,
        Double::class,
        Float::class,
        Long::class,
        Date::class,
        ZonedDateTime::class,
        Boolean::class,

        UUID::class,

        Descriptor::class,
        StringOrMultilingual::class,
        StringOrObject::class,
        IntegerRangeOrValue::class,
    )
}