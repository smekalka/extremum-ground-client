package io.extremum.groundClient.builder.impl

import io.extremum.groundClient.builder.Builders.getById
import io.extremum.groundClient.builder.core.outputField.OutputFieldUtils.allFields
import io.extremum.groundClient.builder.core.outputField.OutputFields.field
import io.extremum.groundClient.builder.core.setOutputFields
import io.extremum.groundClient.model.Account
import io.extremum.groundClient.model.Change
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
                    Account::getChanges,
                    allFields(Change::class)
                )
            ).build("account")

        assertEqual(result, exp)
    }
}