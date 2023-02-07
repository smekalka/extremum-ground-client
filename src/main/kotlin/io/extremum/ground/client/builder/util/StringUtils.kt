package io.extremum.ground.client.builder.util

import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vertx.core.impl.StringEscapeUtils
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.reflect.KClass

object StringUtils {

    /**
     * Первую букву строки переводит в верхний регистр.
     * см. [capitalize]
     */
    fun String?.capitalise(): String? =
        this?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    fun <T : Any> getFieldName(clazz: KClass<T>, fieldGetter: TypedPropertyGetter<T, *>): String =
        PropertyUtils.getPropertyName(clazz.java, fieldGetter)

    fun <T : Any> getFieldType(clazz: KClass<T>, fieldGetter: TypedPropertyGetter<T, *>): Class<*> =
        PropertyUtils.getPropertyDescriptor(clazz.java, fieldGetter).propertyType

    fun ZonedDateTime.toIsoString(): String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this)

    private val sdf = SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ss.SSSSSSZ");
    fun Date.toIsoString(): String = sdf.format(this)

    inline fun <reified T : Any> classNameShort(): String = T::class.simpleName.toString()

    fun String.escape(): String = StringEscapeUtils.escapeJava(this)

    fun String.unescape(): String = StringEscapeUtils.unescapeJava(this)
}