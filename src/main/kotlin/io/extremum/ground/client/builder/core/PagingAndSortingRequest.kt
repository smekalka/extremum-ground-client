package io.extremum.ground.client.builder.core

import de.cronn.reflection.util.TypedPropertyGetter
import io.extremum.ground.client.builder.util.StringUtils.getFieldName
import io.extremum.sharedmodels.basic.BasicModel
import kotlin.reflect.KProperty1

data class PagingAndSortingRequest(
    val limit: Int = 10,
    val offset: Int = 0,
    val orders: List<SortOrder> = listOf()
)

data class SortOrder(
    val direction: Direction? = null,
    val property: String? = null,
) {
    companion object {
        fun <T : BasicModel<*>> sortOrder(direction: Direction, property: KProperty1<T, Any?>): SortOrder =
            SortOrder(direction = direction, property = property.name)

        inline fun <reified T : BasicModel<*>> sortOrder(
            direction: Direction,
            property: TypedPropertyGetter<T, Any?>
        ): SortOrder =
            SortOrder(direction = direction, property = getFieldName(T::class, property))
    }
}

enum class Direction {
    ASC, DESC;
}