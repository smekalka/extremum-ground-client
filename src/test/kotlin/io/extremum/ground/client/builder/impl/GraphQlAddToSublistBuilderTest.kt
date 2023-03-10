package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.Builders.addToSublist
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setOutputFields
import io.extremum.sharedmodels.basic.StringOrObject
import io.extremum.test.tools.StringUtils.assertEqual
import io.extremum.test.tools.StringUtils.wrapWithQuery
import org.junit.jupiter.api.Test

class GraphQlAddToSublistBuilderTest {

    @Test
    fun build() {
        val uuid = "1111-222"
        val exp = wrapWithQuery(
            """
mutation {
    account (
        id: \"$uuid\"
    ) {
        addChanges (
            input: [
                {
                    ordinal: 23.0,
                    compensation: {
                        function: \"function name\",
                        parameters: {
                            param11: \"param11 value\",
                            param22: \"param22 value\"
                        }
                    }
                }
            ],
            paging: {
                offset: 0,
                limit: 10
            }
        ) {
            edges {
                node {
                    uuid
                    ordinal
                    compensation {
                        function
                        parameters
                    }
                }
            }
        }
    }
}
        """
        )

        val result = addToSublist(
            id = uuid,
            sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
            entityToAdd = _root_ide_package_.io.extremum.ground.client.model.Change().apply {
                ordinal = 23.0
                compensation = _root_ide_package_.io.extremum.ground.client.model.Compensation().apply {
                    function = "function name"
                    parameters = StringOrObject(
                        CustomProperties(
                            param11 = "param11 value",
                            param22 = "param22 value",
                        )
                    )
                }
            }
        )
            .setOutputFields(
                field(_root_ide_package_.io.extremum.ground.client.model.Change::getUuid),
                field(_root_ide_package_.io.extremum.ground.client.model.Change::getOrdinal),
                field(
                    _root_ide_package_.io.extremum.ground.client.model.Change::getCompensation,
                    _root_ide_package_.io.extremum.ground.client.model.Compensation::getFunction,
                    _root_ide_package_.io.extremum.ground.client.model.Compensation::getParameters,
                ),
            )
            .build("account")

        assertEqual(result, exp)
    }

    private data class CustomProperties(
        val param11: String,
        val param22: String
    )
}