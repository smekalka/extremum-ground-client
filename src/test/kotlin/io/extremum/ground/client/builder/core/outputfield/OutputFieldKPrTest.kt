package io.extremum.ground.client.builder.core.outputfield

import io.extremum.ground.client.builder.core.PagingAndSortingRequest
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setPaging
import io.extremum.sharedmodels.basic.GraphQlList
import io.extremum.test.tools.StringUtils.assertEqual
import org.junit.jupiter.api.Test

class OutputFieldKPrTest {

    @Test
    fun `single field`() {
        assertEqual(
            actual = field(Level0::level0).toField().build(),
            exp = "level0"
        )
    }

    @Test
    fun `nested field`() {
        // by list of fields
        assertEqual(
            actual = field(
                Level0::level0, listOf(
                    field(Level1::level1_1),
                    field(Level1::level1_2, listOf(field(Level2::level2)))
                )
            )
                .toField()
                .build(),
            exp = """
level0 {
    level1_1
    level1_2 {
        level2
    }
}"""
        )
        // by vararg of fields
        assertEqual(
            actual = field(
                Level0::level0,
                field(Level1::level1_1),
                field(Level1::level1_2, field(Level2::level2))
            )
                .toField()
                .build(),
            exp = """
level0 {
    level1_1
    level1_2 {
        level2
    }
}"""
        )
        // by vararg of properties
        assertEqual(
            actual = field(
                Level0::level0,
                field(Level1::level1_1),
                field(Level1::level1_2, Level2::level2)
            )
                .toField()
                .build(),
            exp = """
level0 {
    level1_1
    level1_2 {
        level2
    }
}"""
        )
    }

    @Test
    fun `field with nested list`() {
        assertEqual(
            actual = field(
                ParentModel::children,
                field(
                    ChildModel::name,
                    Name::first,
                    Name::second
                ),
                field(ChildModel::surname)
            ).setPaging(PagingAndSortingRequest(limit = 2, offset = 3))
                .toField()
                .build(),
            exp = """
children(
    paging: {
        offset: 3,
        limit: 2
    }
) {
    edges {
        node {
            name {
                first
                second
            }
            surname
        }
    }
}"""
        )
    }

    @Test
    fun `field with nested list and without own nested fields`() {
        assertEqual(
            actual = field(
                ParentModel::children
            ).setPaging(PagingAndSortingRequest(limit = 2, offset = 3))
                .toField()
                .build(),
            exp = """
children(
    paging: {
        offset: 3,
        limit: 2
    }
) {
    edges {
        node {
            uuid
        }
    }
}"""
        )
    }

    @Test
    fun `field with nested list and default paging`() {
        assertEqual(
            actual = field(
                ParentModel::children,
                field(
                    ChildModel::name,
                    Name::first,
                    Name::second
                ),
                field(ChildModel::surname)
            )
                .toField()
                .build(),
            exp = """
children(
    paging: {
        offset: 0,
        limit: 10
    }
) {
    edges {
        node {
            name {
                first
                second
            }
            surname
        }
    }
}"""
        )
    }

    private data class Level0(
        val level0: Level1? = null,
    )

    private data class Level1(
        val level1_1: String? = null,
        val level1_2: Level2? = null,
    )

    private data class Level2(
        val level2: String? = null,
    )

    private data class ParentModel(
        val children: GraphQlList<ChildModel>
    ) : _root_ide_package_.io.extremum.ground.client.model.TestBasicModel()

    private data class ChildModel(
        var name: Name? = null,
        var surname: String? = null,
    ) : _root_ide_package_.io.extremum.ground.client.model.TestBasicModel()

    private data class Name(
        val first: String? = null,
        val second: String? = null,
    ) : _root_ide_package_.io.extremum.ground.client.model.TestBasicModel()
}