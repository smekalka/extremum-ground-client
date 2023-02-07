package io.extremum.ground.client.builder.core.outputfield

import io.extremum.ground.client.builder.core.PagingAndSortingRequest
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setPaging
import io.extremum.test.tools.StringUtils.assertEqual
import org.junit.jupiter.api.Test

class OutputFieldStrTest {

    @Test
    fun `single field`() {
        assertEqual(
            actual = field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level0::getLevel0).toField().build(),
            exp = "level0"
        )
    }

    @Test
    fun `nested field`() {
        // by list of fields
        assertEqual(
            actual = field(
                _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level0::getLevel0, listOf(
                    field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_1),
                    field(
                        _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_2, listOf(field(
                            _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level2::getLevel2)))
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
                _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level0::getLevel0,
                field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_1),
                field(
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_2, field(
                        _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level2::getLevel2))
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
        // by string + vararg of fields
        assertEqual(
            actual = field(
                "level0",
                field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_1),
                field(
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_2, field(
                        _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level2::getLevel2))
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
                _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level0::getLevel0,
                field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_1),
                field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level1::getLevel1_2, _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Level2::getLevel2)
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
                _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.ParentModel::getChildren,
                field(
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.ChildModel::getName,
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Name::getFirst,
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Name::getSecond
                ),
                field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.ChildModel::getSurname)
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
    fun `field with nested list and default paging`() {
        assertEqual(
            actual = field(
                _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.ParentModel::getChildren,
                field(
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.ChildModel::getName,
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Name::getFirst,
                    _root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.Name::getSecond
                ),
                field(_root_ide_package_.io.extremum.ground.client.builder.core.outputfield.model.ChildModel::getSurname)
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
}