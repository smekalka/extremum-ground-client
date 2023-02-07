package io.extremum.groundClient.builder.impl

import io.extremum.groundClient.builder.Builders.query
import io.extremum.groundClient.builder.core.Direction
import io.extremum.groundClient.builder.core.PagingAndSortingRequest
import io.extremum.groundClient.builder.core.SortOrder.Companion.sortOrder
import io.extremum.groundClient.builder.core.outputField.OutputFields.field
import io.extremum.groundClient.builder.core.setOutputFields
import io.extremum.groundClient.model.Zone
import io.extremum.test.tools.StringUtils.assertEqual
import io.extremum.test.tools.StringUtils.wrapWithQuery
import org.junit.jupiter.api.Test

class GraphQlQueryBuilderTest {

    @Test
    fun `build query`() {
        val exp = wrapWithQuery(
            """
query {
    zones (
        paging: {
            offset: 0,
            limit: 10
        }
    ) {
        edges {
            node {
                description
                uuid
            }
        }
    }
}
        """
        )

        val result = query()
            .setOutputFields(
                field(Zone::getDescription),
                field(Zone::getUuid),
            )
            .build("zones")

        assertEqual(result, exp)
    }

    @Test
    fun `build query with paging`() {
        val exp = wrapWithQuery(
            """
query {
    zones (
        paging: {
            offset: 10,
            limit: 100
        }
    ) {
        edges {
            node {
                description
                uuid
            }
        }
    }
}
        """
        )

        val result = query(PagingAndSortingRequest(limit = 100, offset = 10))
            .setOutputFields(
                field(Zone::getDescription),
                field(Zone::getUuid),
            )
            .build("zones")

        assertEqual(result, exp)
    }

    @Test
    fun `build query with paging and orders`() {
        val exp = wrapWithQuery(
            """
query {
    zones (
        paging: {
            offset: 10,
            limit: 100,
            orders: [
                {
                    direction: DESC,
                    property: \"uuid\"
                },
                {
                    direction: ASC,
                    property: \"description\"
                }
            ]
        }
    ) {
        edges {
            node {
                description
                uuid
            }
        }
    }
}
        """
        )

        val result = query(
                PagingAndSortingRequest(
                    limit = 100, offset = 10, orders = listOf(
                        sortOrder(direction = Direction.DESC, property = Zone::getUuid),
                        sortOrder(direction = Direction.ASC, property = Zone::getDescription),
                    )
                )
            )
            .setOutputFields(
                field(Zone::getDescription),
                field(Zone::getUuid),
            )
            .build("zones")

        assertEqual(result, exp)
    }

    @Test
    fun `query with filter`() {
        val exp = wrapWithQuery(
            """
query {
    zones (
        paging: {
            offset: 0,
            limit: 10
        },
        filter: \"object.name.eq(\\\"event2\\\")\"
    ) {
        edges {
            node {
                description
                uuid
            }
        }
    }
}
        """
        )

        val result = query(filter = "object.name.eq(\"event2\")")
            .setOutputFields(
                field(Zone::getDescription),
                field(Zone::getUuid),
            )
            .build("zones")

        assertEqual(result, exp)
    }
}