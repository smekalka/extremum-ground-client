package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.Builders.getById
import io.extremum.ground.client.builder.core.outputfield.OutputFieldUtils.allFields
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setOutputFields
import io.extremum.test.tools.StringUtils.assertEqual
import io.extremum.test.tools.StringUtils.wrapWithQuery
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class GraphQlGetByIdBuilderTest {

    @Test
    fun build() {
        val id = randomUUID()
        val exp = wrapWithQuery(
            """
query {
    zone (
         id: $id
    ) {
        uuid
    }
}
        """
        )

        val result = getById(id)
            .build("zone")

        assertEqual(result, exp)
    }

    @Test
    fun `build with nested list in output field`() {
        val id = randomUUID()
        val exp = wrapWithQuery(
            """
query {
    account (
         id: $id
    ) {
        changes (
            paging: {
                offset: 0,
                limit: 10
            }
        ) {
            edges {
                node {
                    ordinal
                    data 
                    compensation {
                        function
                        parameters
                        uuid
                    }
                    uuid
                }
            }
        }
        uuid
    }
}
        """
        )

        val result = getById(id)
            .setOutputFields(
                field(
                    _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                    allFields(_root_ide_package_.io.extremum.ground.client.model.Change::class)
                )
            ).build("account")

        assertEqual(result, exp)
    }
}