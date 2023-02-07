package io.extremum.groundClient.builder.core

import io.extremum.groundClient.builder.util.BuilderUtils.fieldWithProperties
import io.extremum.groundClient.builder.util.BuilderUtils.property
import io.smallrye.graphql.client.core.Argument
import io.smallrye.graphql.client.core.Argument.arg
import io.smallrye.graphql.client.impl.core.InputObjectImpl

interface Pageable {
    var paging: PagingAndSortingRequest?

    var filter: String?

    fun buildPagingAndFilter(): List<Argument> = listOfNotNull(
        buildPaging(),
        if (filter.isNullOrBlank()) {
            null
        } else {
            arg("filter", filter)
        }
    )

    private fun buildPaging(): Argument? {
        val properties = paging?.run {
            listOf(
                property("offset", offset),
                property("limit", limit),
            ).let {
                if (orders.isEmpty()) {
                    it
                } else {
                    it + property(
                        "orders", orders.map {
                            InputObjectImpl().apply {
                                inputObjectFields = listOf(
                                    property("direction", it.direction),
                                    property("property", it.property),
                                )
                            }
                        }.toTypedArray()
                    )
                }
            }
        } ?: return null
        return fieldWithProperties(
            "paging",
            *properties.toTypedArray()
        )
    }
}

fun <T : Pageable> T.setPaging(paging: PagingAndSortingRequest): T {
    this.paging = paging
    return this
}

fun <T : Pageable> T.setFilter(filter: String): T {
    this.filter = filter
    return this
}
