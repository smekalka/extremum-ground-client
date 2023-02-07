package io.extremum.ground.client.client

import io.extremum.ground.client.builder.Builders.addToSublist
import io.extremum.ground.client.builder.Builders.getById
import io.extremum.ground.client.builder.Builders.query
import io.extremum.ground.client.builder.Builders.remove
import io.extremum.ground.client.builder.Builders.removeFromSublist
import io.extremum.ground.client.builder.Builders.update
import io.extremum.ground.client.builder.core.addOutputFields
import io.extremum.ground.client.builder.core.outputfield.OutputFields.field
import io.extremum.ground.client.builder.core.setAllOutputFields
import io.extremum.ground.client.builder.core.setOutputFields
import io.extremum.ground.client.builder.tx.beginTx
import io.extremum.ground.client.builder.tx.commit
import io.extremum.ground.client.builder.tx.inTx
import io.extremum.ground.client.builder.util.StringUtils.classNameShort
import io.extremum.ground.client.client.Response.Status.DATA_FETCHING_EXCEPTION
import io.extremum.ground.client.client.Response.Status.TX_NOT_FOUND
import io.extremum.ground.client.model.Account.AccountDatatype
import io.extremum.model.tools.mapper.GraphQlListUtils.toGraphQlList
import io.extremum.model.tools.mapper.GraphQlListUtils.toList
import io.extremum.model.tools.mapper.MapperUtils.convertValue
import io.extremum.sharedmodels.basic.StringOrObject
import io.extremum.sharedmodels.descriptor.Descriptor
import io.extremum.sharedmodels.structs.IntegerRangeOrValue
import io.extremum.test.tools.AssertionUtils.assertEqualsDescriptors
import io.extremum.test.tools.AssertionUtils.hasFieldWithValue
import io.extremum.test.tools.AssertionUtils.isNotNullExt
import io.extremum.test.tools.StringUtils.toStringOrMultilingual
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import java.time.ZonedDateTime
import java.util.UUID.randomUUID

class GroundApiClientIT {

    private val groundApiClient = GroundApiClient(
        url = GroundProperties.URL,
        headers = mapOf(HttpHeaders.AUTHORIZATION to GroundProperties.TOKEN)
    )

    @Disabled("launched ground application is needed")
    @Test
    fun `get zones`() {
        runBlocking {
            groundApiClient.createEmpty<_root_ide_package_.io.extremum.ground.client.model.Zone>()
            val builder = query()
                .setOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getDescription),
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getUuid)
                )

            val result = groundApiClient.query<_root_ide_package_.io.extremum.ground.client.model.Zone>(builder)
                .validateStatusAndValueNotNull()

            println("result: $result")
            assertThat(result).isNotEmpty
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `find account by filter`() {
        runBlocking {
            val searchingValue = "searching"
            groundApiClient.create(_root_ide_package_.io.extremum.ground.client.model.Compensation().apply {
                function = searchingValue
            })

            val result = groundApiClient.query<_root_ide_package_.io.extremum.ground.client.model.Compensation>(query(filter = "object.function.eq(\"$searchingValue\")"))
                .validateStatusAndValueNotNull()

            println("result: $result")
            assertThat(result).isNotEmpty
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `get zone by id`() {
        runBlocking {
            val zone = groundApiClient.createEmpty<_root_ide_package_.io.extremum.ground.client.model.Zone>()
            val id = zone.uuid
            val builder = getById(id)
                .setOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getDescription),
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getUuid)
                )

            val result = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Zone>(builder)
                .validateStatusAndValueNotNull()

            println("result: $result")
            assertEqualsDescriptors(id, result.uuid)
        }
    }


    @Disabled("launched ground application is needed")
    @Test
    fun `get zone by not existing id`() {
        runBlocking {
            val builder = getById(randomUuidStr())
                .setOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getUuid)
                )

            val (result, status) = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Zone>(builder)

            println("result: $result")
            assertThat(status).isEqualTo(DATA_FETCHING_EXCEPTION)
            assertThat(result).isNull()
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `get account by id without builder`() {
        runBlocking {
            val value = "base value"
            val createdAccount = createAccount(value)
            println("createdAccount: $createdAccount")

            val id = createdAccount.uuid

            val result = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Account>(id)

            println("result: $result")
            assertThat(result)
                .isNotNullExt()
                .hasFieldWithValue(_root_ide_package_.io.extremum.ground.client.model.Account::getValue, value)
            assertEqualsDescriptors(id, result!!.uuid)
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `create zone`() {
        runBlocking {
            val description = "description via client".toStringOrMultilingual()
            val builder = update()
                .setInput(
                    _root_ide_package_.io.extremum.ground.client.model.Zone().apply {
                        this.description = description
                        created = ZonedDateTime.now()
                    }
                )
                .addOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getDescription),
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getCreated),
                )

            val result = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Zone>(builder)
                .validateStatusAndValueNotNull()

            println("result: $result")
            assertThat(result)
                .hasFieldWithValue(_root_ide_package_.io.extremum.ground.client.model.Zone::getDescription, description)
            assertThat(result.created).isNotNull
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `create event with IntegerRangeOrValue`() {
        runBlocking {
            val participants = IntegerRangeOrValue(10, 20)
            val builder = update()
                .setInput(_root_ide_package_.io.extremum.ground.client.model.Event().apply {
                    this.participants = participants
                })
                .addOutputFields(field(_root_ide_package_.io.extremum.ground.client.model.Event::getParticipants))


            val result = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Event>(builder)
                .validateStatusAndValueNotNull()

            println("result: $result")
            val savedParticipants = result.participants
            assertThat(savedParticipants).isNotNull
            assertThat(savedParticipants.min).isEqualTo(participants.min)
            assertThat(savedParticipants.max).isEqualTo(participants.max)
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `create event with nested fields`() {
        runBlocking {
            val event = _root_ide_package_.io.extremum.ground.client.model.Event().apply {
                url = "event url"
                size = 23
                product = _root_ide_package_.io.extremum.ground.client.model.Product().apply {
                    name = "bottle".toStringOrMultilingual()
                    rating = 8.3
                }
                experiences = listOf(
                    _root_ide_package_.io.extremum.ground.client.model.Experience().apply {
                        mime = "mime1"
                    },
                    _root_ide_package_.io.extremum.ground.client.model.Experience().apply {
                        mime = "mime2"
                    }
                ).toGraphQlList()
            }
            val builder = update()
                .setInput(event)
                .addOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Event::getUrl),
                    field(_root_ide_package_.io.extremum.ground.client.model.Event::getSize),
                )

            val result = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Event>(builder)
                .validateStatusAndValueNotNull()

            println("result: $result")
            assertThat(result)
                .hasFieldWithValue(_root_ide_package_.io.extremum.ground.client.model.Event::getUrl, event.url)
                .hasFieldWithValue(_root_ide_package_.io.extremum.ground.client.model.Event::getSize, event.size)
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `create account`() {
        runBlocking {
            val value = "base value"
            val result = createAccount(value)

            println("result: $result")
            assertThat(result)
                .isNotNullExt()
                .hasFieldWithValue(_root_ide_package_.io.extremum.ground.client.model.Account::getValue, value)
        }
    }

    private suspend fun createAccount(value: String = "base value"): _root_ide_package_.io.extremum.ground.client.model.Account {
        val builder = update()
            .setInput(
                _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                    datatype = AccountDatatype.STRING_ARRAY
                    this.value = value
                }
            )
            .addOutputFields(
                field(_root_ide_package_.io.extremum.ground.client.model.Account::getDatatype),
                field(_root_ide_package_.io.extremum.ground.client.model.Account::getValue),
            )

        val response = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Account>(builder)
        return response.validateStatusAndValueNotNull("account")
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `update account`() {
        runBlocking {
            val value = "base value"
            val createdAccount = createAccount(value)
            println("createdAccount: $createdAccount")

            val id = createdAccount.uuid

            val updatedValue = "updated value"
            val builder = update(id)
                .setInput(
                    _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                        datatype = AccountDatatype.CUSTOM
                        this.value = updatedValue
                    }
                )
                .addOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Account::getDatatype),
                    field(_root_ide_package_.io.extremum.ground.client.model.Account::getValue),
                )

            val result = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Account>(builder)
                .validateStatusAndValueNotNull()

            println("result: $result")
            assertThat(result)
                .hasFieldWithValue(_root_ide_package_.io.extremum.ground.client.model.Account::getValue, updatedValue)
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `update not existing account`() {
        runBlocking {
            val builder = update(randomUuidStr())
                .setInput(
                    _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                        datatype = AccountDatatype.CUSTOM
                    }
                )

            val (result, status) = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Account>(builder)

            println("result: $result")
            assertThat(status).isEqualTo(DATA_FETCHING_EXCEPTION)
            assertThat(result).isNull()
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `remove account by builder`() {
        runBlocking {
            val createdAccount = createAccount()
            println("createdAccount: $createdAccount")

            val id = createdAccount.uuid
            val builder = remove(id)

            val result = groundApiClient.remove(builder)
                .validateStatusAndValueNotNull()

            println("result: $result")
            assertThat(result).isTrue
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `remove account by id`() {
        runBlocking {
            val createdAccount = createAccount()
            println("createdAccount: $createdAccount")

            val result = groundApiClient.remove(createdAccount.uuid)

            assertThat(result).isTrue
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `remove not existing account`() {
        runBlocking {
            val builder = remove(randomUuidStr())

            val (result, status) = groundApiClient.remove(builder)

            println("result: $result")
            assertThat(status).isEqualTo(DATA_FETCHING_EXCEPTION)
            assertThat(result).isFalse
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `add changes`() {
        runBlocking {
            val createdAccount = createAccount()
            println("createdAccount: $createdAccount")

            val id = createdAccount.uuid
            val function = "function name"
            val parameters = CustomProperties(
                param11 = "param11 value",
                param22 = "param22 value",
            )
            val result = addChange(id, function, parameters)

            println("result: $result")
            assertThat(result).isNotEmpty
            val createdChange = result[0]
            assertThat(createdChange).hasFieldOrProperty("compensation")
            val compensation = createdChange.compensation
            assertThat(compensation)
                .isNotNullExt()
                .hasFieldWithValue(_root_ide_package_.io.extremum.ground.client.model.Compensation::getFunction, function)
                .hasFieldOrProperty("parameters")
            val savedParameters = compensation!!.parameters.`object`
            val savedCustomProperties = savedParameters.convertValue<CustomProperties>()
            assertThat(savedCustomProperties).isEqualTo(parameters)
        }
    }

    private suspend fun addChange(
        accountId: Descriptor,
        function: String = "function name",
        parameters: CustomProperties = CustomProperties(
            param11 = "param11 value",
            param22 = "param22 value",
        )
    ): List<_root_ide_package_.io.extremum.ground.client.model.Change> {
        val builder = addToSublist(
            id = accountId,
            sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
            entityToAdd = _root_ide_package_.io.extremum.ground.client.model.Change().apply {
                ordinal = 23.0
                compensation = _root_ide_package_.io.extremum.ground.client.model.Compensation().apply {
                    this.function = function
                    this.parameters = StringOrObject(parameters)
                }
            }
        )
            .setAllOutputFields(_root_ide_package_.io.extremum.ground.client.model.Change::class)

        val response = groundApiClient.updateSublist<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Change>(builder)
        return response.validateStatusAndValueNotNull(classNameShort<_root_ide_package_.io.extremum.ground.client.model.Change>())
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `add several changes and get from sublist`() {
        runBlocking {
            val createdAccount = createAccount()
            val entitiesToAdd = listOf(
                _root_ide_package_.io.extremum.ground.client.model.Change().apply {
                    ordinal = 1.1
                },
                _root_ide_package_.io.extremum.ground.client.model.Change().apply {
                    ordinal = 2.1
                }
            )
            val response = groundApiClient.updateSublist<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Change>(
                addToSublist(
                    id = createdAccount.uuid,
                    sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                    entitiesToAdd = entitiesToAdd
                )
            )
            response.validateStatus(classNameShort<_root_ide_package_.io.extremum.ground.client.model.Change>())

            val getAccountResponse = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Account>(
                getById(createdAccount.uuid)
                    .setOutputFields(field(
                        _root_ide_package_.io.extremum.ground.client.model.Account::getChanges, field(
                            _root_ide_package_.io.extremum.ground.client.model.Change::getOrdinal)))
            )
            val account = getAccountResponse.validateStatusAndValueNotNull(classNameShort<_root_ide_package_.io.extremum.ground.client.model.Account>())
            assertThat(account.changes).isNotNull
            val changes = account.changes.toList()
            assertThat(changes).hasSize(2)
            assertThat(changes.map { it.ordinal })
                .containsExactlyInAnyOrder(*entitiesToAdd.map { it.ordinal }.toTypedArray())

        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `get account changes`() {
        runBlocking {
            // создание аккаунта
            val createdAccount = createAccount()
            println("createdAccount: $createdAccount")

            // добавление в список 2 элементов
            val id = createdAccount.uuid
            val function1 = randomUuidStr()
            val function2 = randomUuidStr()
            addChange(id, function1)
            addChange(id, function2)

            // получение списка в этом Account
            val getByIdBuilder = getById(id)
                .setOutputFields(
                    field(
                        _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                        field(_root_ide_package_.io.extremum.ground.client.model.Change::getCompensation, _root_ide_package_.io.extremum.ground.client.model.Compensation::getFunction),
                        field(_root_ide_package_.io.extremum.ground.client.model.Change::getUuid)
                    )
                )
            val getAccountResponse = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Account>(getByIdBuilder)
            val result = getAccountResponse.validateStatusAndValueNotNull(classNameShort<_root_ide_package_.io.extremum.ground.client.model.Account>())
            println("received account with changes: $result")

            val changes = result.changes.toList()
            assertThat(changes).hasSize(2)
            assertThat(changes.map { it.compensation.function }).containsExactlyInAnyOrder(function1, function2)
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `remove changes`() {
        runBlocking {
            // создание аккаунта
            val createdAccount = createAccount()
            println("createdAccount: $createdAccount")

            // добавление в список
            val id = createdAccount.uuid
            val function = randomUuidStr()
            val addChangesResult = addChange(id, function)
            println("addChangesResult: $addChangesResult")
            val addedChangesId = addChangesResult.find { it.compensation?.function == function }?.uuid

            assertThat(addedChangesId).isNotNull

            // удаление из списка - проверяемое действие
            val builder = removeFromSublist(
                id = id,
                sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                idToRemove = addedChangesId!!
            )
                .addOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Change::getUuid),
                )

            val result = groundApiClient.updateSublist<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Change>(builder)

            println("result: $result")
            val resultNotNull = result.validateStatusAndValueNotNull()
            val removed = resultNotNull.find { it.uuid == addedChangesId }
            assertThat(removed).isNull()
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `begin tx`() {
        runBlocking {
            val txId = groundApiClient.beginTx().validateStatusAndTx("begin tx").txId
            val zone = groundApiClient.createEmpty<_root_ide_package_.io.extremum.ground.client.model.Zone>(txId)

            groundApiClient.commit(txId)

            val afterCommit = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Zone>(zone.uuid)
            assertThat(afterCommit).isNotNull
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `tx commit`() {
        runBlocking {
            // начало транзакции
            val builder = query()
                .setOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getDescription),
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getUuid)
                )
                .beginTx()

            val beginTxResponse = groundApiClient.query<_root_ide_package_.io.extremum.ground.client.model.Zone>(builder)
            val txId = beginTxResponse.validateStatusAndTx().txId

            // внутри транзакции: создание аккаунта
            val createBuilder = update()
                .setInput(
                    _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                        datatype = AccountDatatype.STRING_ARRAY
                        value = "account value"
                    }
                )
                .addOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Account::getDatatype),
                    field(_root_ide_package_.io.extremum.ground.client.model.Account::getValue),
                )
                .inTx(txId)

            val createdAccountResponse = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Account>(createBuilder)
            val createdAccount = createdAccountResponse.validateStatusAndValueNotNull(classNameShort<_root_ide_package_.io.extremum.ground.client.model.Account>())
            val accountId = createdAccount.uuid

            // commit транзакции при добавлении changes в account
            val addToSublistBuilder = addToSublist(
                id = accountId,
                sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                entityToAdd = _root_ide_package_.io.extremum.ground.client.model.Change().apply {
                    ordinal = 23.0
                }
            )
                .addOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Change::getUuid),
                )
                .commit(txId)

            groundApiClient.updateSublist<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Change>(addToSublistBuilder)
            // альтернатива commit-а без запроса
            // groundApiClient.commit(txId)

            // проверка, что аккаунт существует - commit сработал
            val account = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Account>(accountId)

            println("account: $account")
            assertThat(account).isNotNull
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `tx block`() {
        runBlocking {
            val accountId = groundApiClient.tx { txId ->
                // внутри транзакции: создание аккаунта
                val createdAccount = groundApiClient.create(
                    value = _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                        datatype = AccountDatatype.STRING_ARRAY
                        value = "account value"
                    },
                    inTxId = txId
                )
                val accountId = createdAccount.uuid

                // добавление changes в account
                val addToSublistBuilder = addToSublist(
                    id = accountId,
                    sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                    entityToAdd = _root_ide_package_.io.extremum.ground.client.model.Change().apply {
                        ordinal = 23.0
                    }
                )
                    .addOutputFields(
                        field(_root_ide_package_.io.extremum.ground.client.model.Change::getUuid),
                    )
                    .inTx(txId)
                groundApiClient.updateSublist<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Change>(addToSublistBuilder)

                accountId
            }

            // проверка, что аккаунт существует - commit сработал
            val account = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Account>(accountId)

            println("account: $account")
            assertThat(account).isNotNull
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `commit not existing tx`() {
        runBlocking {
            val (commitSuccess, status) = groundApiClient.commit(randomUuidStr())

            assertThat(commitSuccess).isFalse
            assertThat(status).isEqualTo(TX_NOT_FOUND)
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `commit not existing tx in tx block`() {
        runBlocking {
            // при commit без операций в транзакции не должно быть падения блока
            val result = groundApiClient.tx(
                block = {
                    true
                },
                onError = {
                    throw IllegalStateException("There must be no exception")
                }
            )
            assertThat(result).isTrue
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `commit when nothing to commit`() {
        runBlocking {
            // начало транзакции
            val builder = query()
                .setOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getDescription),
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getUuid)
                )
                .beginTx()

            val beginTxResponse = groundApiClient.query<_root_ide_package_.io.extremum.ground.client.model.Zone>(builder)
            val txId = beginTxResponse.validateStatusAndTx().txId

            // commit без действий, которые нуждаются в commit-е
            val commitSuccess = groundApiClient.commit(txId)
                .validateStatusAndValueNotNull()

            assertThat(commitSuccess).isTrue

            // повторный commit
            val reCommitSuccess = groundApiClient.commit(txId)
                .validateStatusAndValueNotNull()

            assertThat(reCommitSuccess).isTrue
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `tx rollback`() {
        runBlocking {
            // начало транзакции
            val builder = query()
                .setOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getDescription),
                    field(_root_ide_package_.io.extremum.ground.client.model.Zone::getUuid)
                )
                .beginTx()

            val beginTxResponse = groundApiClient.query<_root_ide_package_.io.extremum.ground.client.model.Zone>(builder)
            val txId = beginTxResponse.validateStatusAndTx().txId

            // внутри транзакции: создание аккаунта
            val createBuilder = update()
                .setInput(
                    _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                        datatype = AccountDatatype.STRING_ARRAY
                        value = "account value"
                    }
                )
                .addOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Account::getDatatype),
                    field(_root_ide_package_.io.extremum.ground.client.model.Account::getValue),
                )
                .inTx(txId)

            val createdAccountResponse = groundApiClient.update<_root_ide_package_.io.extremum.ground.client.model.Account>(createBuilder)
            val createdAccount = createdAccountResponse.validateStatusAndValueNotNull(classNameShort<_root_ide_package_.io.extremum.ground.client.model.Account>())
            val accountId = createdAccount.uuid

            // rollback
            val rollbackResult = groundApiClient.rollback(txId).value
            assertThat(rollbackResult).isTrue

            // проверка, что аккаунт не существует - rollback сработал
            val getByIdBuilder = getById(accountId)
                .setOutputFields(
                    field(_root_ide_package_.io.extremum.ground.client.model.Account::getUuid)
                )

            val account = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Account>(getByIdBuilder).value
            println("account: $account")
            assertThat(account).isNull()

            // повторный rollback - ок, так как транзакция существовала
            val rollbackResult2 = groundApiClient.rollback(txId).value
            assertThat(rollbackResult2).isTrue
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `rollback not existing tx`() {
        runBlocking {
            val (rollbackSuccess, status) = groundApiClient.rollback(randomUuidStr())

            assertThat(rollbackSuccess).isFalse
            assertThat(status).isEqualTo(TX_NOT_FOUND)
        }
    }

    @Disabled("launched ground application is needed")
    @Test
    fun `in not existing tx`() {
        runBlocking {
            // inTx с несуществующей транзакцией работает как beginTx для указанной транзакции
            val txId = randomUuidStr()
            val zone = groundApiClient.createEmpty<_root_ide_package_.io.extremum.ground.client.model.Zone>(txId)

            groundApiClient.commit(txId)

            val afterCommit = groundApiClient.getById<_root_ide_package_.io.extremum.ground.client.model.Zone>(zone.uuid)
            assertThat(afterCommit).isNotNull
        }
    }

    private fun randomUuidStr(): String = randomUUID().toString()

    private data class CustomProperties(
        val param11: String,
        val param22: String
    )
}