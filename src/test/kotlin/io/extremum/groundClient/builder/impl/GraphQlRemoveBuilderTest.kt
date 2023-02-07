package io.extremum.groundClient.builder.impl

import io.extremum.groundClient.builder.Builders.remove
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
         id: $id
    )
}
        """
        )

        val result = remove(id)
            .build("zone")

        assertEqual(result, exp)
    }
}