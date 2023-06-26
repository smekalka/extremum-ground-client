package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.Builders.update
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setAllOutputFields
import io.extremum.ground.client.builder.core.setOutputFields
import io.extremum.ground.client.model.Account
import io.extremum.ground.client.model.Change
import io.extremum.ground.client.model.Compensation
import io.extremum.ground.client.model.Event
import io.extremum.ground.client.model.Experience
import io.extremum.ground.client.model.Product
import io.extremum.ground.client.model.Zone
import io.extremum.model.tools.mapper.GraphQlListUtils.toGraphQlList
import io.extremum.model.tools.mapper.MapperUtils.copy
import io.extremum.sharedmodels.basic.BasicModel
import io.extremum.sharedmodels.basic.GraphQlList
import io.extremum.sharedmodels.descriptor.Descriptor
import io.extremum.test.tools.StringUtils.assertEqual
import io.extremum.test.tools.StringUtils.toDescriptor
import io.extremum.test.tools.StringUtils.toStringOrMultilingual
import io.extremum.test.tools.StringUtils.wrapWithQuery
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

class GraphQlUpdateBuilderTest {

    @Test
    fun `build create`() {
        val description = "custom description"
        val exp = wrapWithQuery(
            """
mutation {
    zone (
         input: {
            description: \"$description\"
         }
    ) {
        uuid
    }
}
        """
        )

        val result = update()
            .setInput(Zone().apply { this.description = description.toStringOrMultilingual() })
            .build("zone")

        assertEqual(result, exp)
    }

    @Test
    fun `build create with custom output fields`() {
        val description = "custom description"
        val size = 20
        val created = ZonedDateTime.parse("2022-11-21T19:10:00.348997+07:00")
        val exp = wrapWithQuery(
            """
mutation {
    zone (
        input: {
            description: \"$description\",
            size: $size,
            created:\"2022-11-21T19:10:00.348997+07:00\"
        }
    ) {
        description
        size
        uuid
    }
}
        """
        )

        val result = update()
            .setInput(
                Zone().apply {
                    this.description = description.toStringOrMultilingual()
                    this.size = size
                    this.created = created
                }
            )
            .setOutputFields(
                field(Zone::getDescription),
                field(Zone::getSize),
            )
            .build("zone")

        assertEqual(result, exp)
    }

    @Test
    fun `build create with nested fields`() {
        val exp = wrapWithQuery(
            """
mutation {
    event (
        input: {
            url: \"event url\",
            size: 23,
            product: {
                name: \"bottle\",
                rating:8.3
            },
            experiences: [
                {
                    mime: \"mime1\"
                },
                {
                    mime: \"mime2\"
                }
            ]
        }
    ) {
        url
        size
        product {
            name
            rating
        }
        experiences (
            paging: {
                offset: 0,
                limit: 10
            }
        ) {
            edges {
                node {
                    mime
                }
            }
        }
        uuid
    }
}
        """
        )

        val event = Event().apply {
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
                },
            ).toGraphQlList()
        }

        val result = update()
            .setInput(event)
            .setOutputFields(
                field(Event::getUrl),
                field(Event::getSize),
                field(
                    Event::getProduct, field(Product::getName), field(
                        Product::getRating)),
                field(Event::getExperiences, field(Experience::getMime)),
            )
            .build("event")

        assertEqual(result, exp)
    }

    @Test
    fun `build update`() {
        val uuid = "1111-222"
        val value = "new value"
        val exp = wrapWithQuery(
            """
mutation {
    account (
        uuid: \"$uuid\",
        input: {
            value: \"$value\"
        }
    ) {
        uuid
    }
}
        """
        )

        val result = update(uuid)
            .setInput(
                entity = Account().apply {
                    this.value = value
                    this.uuid = Descriptor("333")
                },
                Account::getValue
            )
            .build("account")

        assertEqual(result, exp)
    }

    @Test
    fun `build update to null value`() {
        val uuid = "1111-222"
        val exp = wrapWithQuery(
            """
mutation {
    account (
        uuid: \"$uuid\",
        input: {
            value: null
        }
    ) {
        uuid
    }
}
        """
        )

        val result = update(uuid)
            .setInput(entity = Account().apply { this.uuid = "333".toDescriptor() }, Account::getValue)
            .build("account")

        assertEqual(result, exp)
    }

    @Test
    fun `build update ignore values without data`() {
        val uuid = "1111-222"
        val ordinal = 20.0
        val function = "func"
        val exp = wrapWithQuery(
            """
mutation {
    change (
        uuid: \"$uuid\",
        input: {
            ordinal: $ordinal,
            compensation: {
                function: \"$function\",
                parameters: null
            }
        }
    ) {
        uuid
    }
}
        """
        )

        val result = update(uuid)
            .setInput(
                entity = Change().apply {
                    this.uuid = "333".toDescriptor()
                    this.ordinal = ordinal
                    compensation = Compensation().apply { this.function = function }
                }
            )
            .build("change")

        assertEqual(result, exp)
    }

    @Test
    fun `build update ignore values without data with enum`() {
        val uuid = "1111-222"
        val exp = wrapWithQuery(
            """
mutation {
    account (
        uuid: \"$uuid\",
        input: {
            datatype: NUMBER
        }
    ) {
        uuid
    }
}
        """
        )

        val result = update(uuid)
            .setInput(entity = Account().apply {
                this.uuid = "333".toDescriptor()
                datatype = Account.AccountDatatype.NUMBER
            })
            .build("account")
        assertEqual(result, exp)
    }

    @Test
    fun `build update with all output fields`() {
        val uuid = "1111-222"
        val exp = wrapWithQuery(
            """
mutation {
    event (
        uuid: \"$uuid\",
        input: {
            url: \"event url\"
        }
    ) {
        url
        size
        product {
            name
            rating
            uuid
        }
        participants
        uuid
    }
}
        """
        )

        val event = Event().apply {
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

        val result = update(uuid)
            .setInput(event, Event::getUrl)
            .setAllOutputFields(Event::class.java)
            .build("event")

        assertEqual(result, exp)
    }

    @Test
    fun `build create with all output fields nested model`() {
        val name = "model name"
        val exp = wrapWithQuery(
            """
mutation {
    model (
        input: {
            name: \"$name\"
        }
    ) {
        iri
        name
        uuid
        uuid1
    }
}
        """
        )

        val model = Model(
            name = name,
        )

        val result = update()
            .setInput(model, Model::name)
            .setAllOutputFields(Model::class.java)
            .build("model")

        assertEqual(result, exp)
    }

    private data class Model(
        var uuid1: Descriptor? = null,
        val name: String,
        val child: Model? = null,
        private var iri: String? = null,
        private var uuid: UUID? = null,
    ) : BasicModel<UUID> {

        override fun getId(): UUID? {
            return uuid
        }

        override fun getIri(): String? {
            return if (this.uuid != null) {
                if (uuid1?.iri != null) uuid1?.iri else iri
            } else {
                iri
            }
        }

        override fun setUuid(uuid: Descriptor) {
            uuid1 = uuid
        }

        override fun setIri(iri: String?) {
            this.iri = iri
        }

        override fun setId(uuid: UUID?) {
            this.uuid = uuid
        }

        override fun getUuid(): Descriptor? {
            return uuid1
        }
    }

    @Test
    fun `set input by 2 entities`() {
        val uuid = "1111-222"

        val exp = wrapWithQuery(
            """
mutation {
    event (
        uuid: \"$uuid\",
        input: {
            url: \"new url\",
            size: null,
            product: {
                name: \"new name\"
            },
            experiences:[]
        }
    ) {
        uuid
    }
}
        """
        )
        val event1 = Event().apply {
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

        val result = update(uuid)
            .setInputForUpdating(
                event1,
                event1.copy {
                    url = "new url"
                    size = null
                    product = event1.product?.copy { name = "new name".toStringOrMultilingual() }
                    experiences = GraphQlList()
                }
            ).build("event")

        assertEqual(result, exp)
    }

    @Test
    fun `set input by 2 entities with list`() {
        val uuid = "1111-222"
        val uuidExperience = "1111-333".toDescriptor()

        val exp = wrapWithQuery(
            """
mutation {
    event (
        uuid: \"$uuid\",
        input: {
            url: \"new url\"
        }
    ) {
        uuid
    }
}
        """
        )
        val event1 = Event().apply {
            size = 23
            experiences = listOf(
                Experience().apply {
                    this.uuid = uuidExperience
                },
            ).toGraphQlList()
        }

        val result = update(uuid)
            .setInputForUpdating(
                event1,
                event1.copy {
                    url = "new url"
                }
            ).build("event")

        assertEqual(result, exp)

    }
}