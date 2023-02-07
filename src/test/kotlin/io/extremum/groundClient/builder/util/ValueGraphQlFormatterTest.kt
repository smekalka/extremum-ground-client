package io.extremum.groundClient.builder.util

import io.extremum.groundClient.builder.util.ValueGraphQlFormatter.format
import io.extremum.groundClient.model.Event
import io.extremum.groundClient.model.Experience
import io.extremum.groundClient.model.Product
import io.extremum.model.tools.mapper.GraphQlListUtils.toGraphQlList
import io.extremum.test.tools.StringUtils.assertEqual
import io.extremum.test.tools.StringUtils.toStringOrMultilingual
import org.junit.jupiter.api.Test

class ValueGraphQlFormatterTest {

    private val event = Event().apply {
        url = "event url"
        size = 23
        product = Product().apply {
            name = "bottle".toStringOrMultilingual()
            rating = 8.3
        }
        experiences = listOf(
            Experience().apply {
                mime = "mime1"
            },
            Experience().apply {
                mime = "mime2"
            }
        ).toGraphQlList()
    }

    @Test
    fun format() {
        val exp = """
{
    url: "event url",
    size: 23,
    product: {
        name: "bottle",
        rating: 8.3
    },
    experiences: [
        {
            mime: "mime1"
        },
        {
            mime: "mime2"
        }
    ]
}
            """
        assertEqual(event.format(), exp)
    }

    @Test
    fun `format filter null = false`() {
        val exp = """
{
    url: "event url",
    size: 23,
    product: 
    {
        name: "bottle",
        rating: 8.3
    },
    experiences: [
        {
            mime: "mime1"
        },
        {
            mime: "mime2"
        }
    ], 
    participants: null
}
            """
        assertEqual(event.format(filterEmpty = false), exp)
    }
}