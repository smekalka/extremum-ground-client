package io.extremum.groundClient.builder.impl

import io.extremum.groundClient.builder.Builders.removeFromSublist
import io.extremum.groundClient.builder.core.outputField.OutputFields.field
import io.extremum.groundClient.builder.core.setOutputFields
import io.extremum.groundClient.model.Account
import io.extremum.groundClient.model.Change
import io.extremum.groundClient.model.Compensation
import io.extremum.sharedmodels.descriptor.Descriptor
import io.extremum.test.tools.StringUtils.assertEqual
import io.extremum.test.tools.StringUtils.wrapWithQuery
import org.junit.jupiter.api.Test

class GraphQlRemoveFromSublistBuilderTest {

    @Test
    fun build() {
        val uuidId = "1111-222"
        val uuid = Descriptor(uuidId)
        val uuidToRemove = "333-444"
        val exp = wrapWithQuery(
            """
mutation {
    account (
        id: \"$uuidId\"
    ) {
        removeChanges (
            input: {
                uuid: \"$uuidToRemove\"
            },
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

        val result = removeFromSublist(
            id = uuid,
            sublistFieldGetter = Account::getChanges,
            idToRemove = uuidToRemove
        )
            .setOutputFields(
                field(Change::getUuid),
                field(Change::getOrdinal),
                field(
                    Change::getCompensation,
                    Compensation::getFunction,
                    Compensation::getParameters,
                ),
            )
            .build("account")

        assertEqual(result, exp)
    }
}