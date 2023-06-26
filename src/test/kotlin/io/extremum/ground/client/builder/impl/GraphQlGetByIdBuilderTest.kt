package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.Builders.getById
import io.extremum.ground.client.builder.core.outputfield.OutputFieldUtils.allFields
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setOutputFields
import io.extremum.ground.client.model.Account
import io.extremum.ground.client.model.Change
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
         uuid: $id
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
         uuid: $id
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
                    Account::getChanges,
                    allFields(Change::class)
                )
            ).build("account")

        assertEqual(result, exp)
    }
}