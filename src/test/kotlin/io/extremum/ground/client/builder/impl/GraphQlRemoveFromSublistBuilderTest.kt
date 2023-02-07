package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.Builders.removeFromSublist
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setOutputFields
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
            sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
            idToRemove = uuidToRemove
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
}