package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.Builders.remove
import io.extremum.test.tools.StringUtils.assertEqual
import io.extremum.test.tools.StringUtils.wrapWithQuery
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class GraphQlRemoveBuilderTest {

    @Test
    fun build() {
        val id = randomUUID()
        val exp = wrapWithQuery(
            """
mutation {
    delete (
         uuid: $id
    )
}
        """
        )

        val result = remove(id)
            .build("zone")

        assertEqual(result, exp)
    }
}