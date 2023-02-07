package io.extremum.ground.client.builder.util

import io.extremum.ground.client.builder.constant.BuilderConstants.ID_FIELD_NAME
import io.extremum.ground.client.builder.util.StringUtils.toIsoString
import io.extremum.model.tools.mapper.GraphQlListUtils.toList
import io.extremum.model.tools.mapper.MapperUtils.convertToMap
import io.extremum.model.tools.mapper.MapperUtils.hasData
import io.extremum.sharedmodels.basic.GraphQlList
import io.extremum.sharedmodels.basic.StringOrMultilingual
import io.extremum.sharedmodels.basic.StringOrObject
import io.extremum.sharedmodels.descriptor.Descriptor
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import kotlin.reflect.full.isSubclassOf

/*
 * Представляет объект в формате, подходящим для input значений в graphQL
 * Пример: из
Event(
    url = "event url",
    size = 23,
    product = Product(
        name = "bottle",
        rating = 8.3
    ),
    experiences = listOf(
        Experience(
            mime = "mime1"
        ),
        Experience(
            mime = "mime2"
        ),
    )
)
формирует
{url: "event url", size: 23, product: {name: "bottle", rating: 8.3}, experiences: [{mime: "mime1"},{mime: "mime2"}]}
 * Модифицированный [io.smallrye.graphql.client.impl.core.utils.ValueFormatter] (io.smallrye:smallrye-graphql-client:1.7.1)
 */
object ValueGraphQlFormatter {
    fun Any?.format(filterEmpty: Boolean = true): String =
        when {
            this == null -> "null"
            this is Collection<*> -> processCollection(this, filterEmpty)
            this is GraphQlList<*> -> processCollection(this.toList(), filterEmpty)
            this is String -> getAsQuotedString(this.toString())
            this is Char -> getAsQuotedString(this.toString())
            this is LocalDate -> getAsQuotedString(this.toString())
            this is ZonedDateTime -> getAsQuotedString(this.toIsoString())
            this is Date -> getAsQuotedString(this.toIsoString())
            this is Int -> this.toString()
            this is Double -> this.toString()
            this is Float -> this.toString()
            this is Long -> this.toString()
            this is Boolean -> this.toString()
            this::class.isSubclassOf(Enum::class) -> (this as Enum<*>).name

            this is UUID -> this.toString()
            this is Descriptor -> getAsQuotedString(this.externalId)
            this is StringOrMultilingual -> processStringOrMultilingual(this, filterEmpty)
            this is StringOrObject<*> -> processStringOrObject(this, filterEmpty)

            this is Map<*, *> -> processMap(this, filterEmpty)
            else -> processObject(this, filterEmpty)
        }

    private fun processStringOrMultilingual(stringOrMultilingual: StringOrMultilingual, filterEmpty: Boolean): String =
        with(stringOrMultilingual) {
            if (isTextOnly) text.format(filterEmpty) else multilingualContent.format(filterEmpty)
        }

    private fun processStringOrObject(stringOrObject: StringOrObject<*>, filterEmpty: Boolean): String =
        with(stringOrObject) {
            if (isSimple) string.format(filterEmpty) else `object`.format(filterEmpty)
        }

    private fun processCollection(collection: Collection<*>, filterEmpty: Boolean): String =
        "[" + collection.joinToString(",") { it.format(filterEmpty) } + "]"

    private fun processMap(map: Map<*, *>, filterEmpty: Boolean): String {
        val properties = map.let {
            if (filterEmpty) {
                it.filterValues { value ->
                    value.hasData()
                }
            } else {
                it
            }
        }.filter { (key, value) ->
            // нельзя задавать uuid = null
            // todo но, возможно, это лишнее и должно в итоге работать без этого исключения
            key != ID_FIELD_NAME || value != null
        }.map { (key, value) -> propertyWithValue(key as String, value, filterEmpty) }
        return "{" + properties.joinToString(", ") + "}"
    }

    private fun propertyWithValue(key: String, value: Any?, filterEmpty: Boolean): String =
        key + ": " + value.format(filterEmpty)

    private fun processObject(value: Any, filterEmpty: Boolean): String = processMap(value.convertToMap(), filterEmpty)

    private fun getAsQuotedString(value: String): String {
        val builder = java.lang.StringBuilder()
        builder.append('"')
        for (c in value.toCharArray()) {
            when (c) {
                '"', '\\' -> {
                    builder.append('\\')
                    builder.append(c)
                }

                '\r' -> builder.append("\\r")
                '\n' -> builder.append("\\n")
                else -> if (c.toInt() < 0x20) {
                    builder.append(String.format("\\u%04x", c.toInt()))
                } else {
                    builder.append(c)
                }
            }
        }
        builder.append('"')
        return builder.toString()
    }
}