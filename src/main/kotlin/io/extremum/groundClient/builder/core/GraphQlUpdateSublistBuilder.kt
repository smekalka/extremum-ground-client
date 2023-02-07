package io.extremum.groundClient.builder.core

import io.extremum.groundClient.builder.util.StringUtils.capitalise

/**
 * Изменяем дочерний список у сущности с id [id]
 */
abstract class GraphQlUpdateSublistBuilder(val id: Any) : GraphQlBuilder(), Pageable {

    override val action: Action = Action.UPDATE_SUBLIST

    override var paging: PagingAndSortingRequest? = PagingAndSortingRequest()

    override var filter: String? = null

    /**
     * Название функции-мутации списка
     */
    var sublistMutationName: String? = null
        set(value) {
            field = getMutationSublistName(value)
        }

    private fun getMutationSublistName(sublistFieldName: String?): String =
        "${getMutationName()}${sublistFieldName.capitalise()}"

    protected abstract fun getMutationName(): String
}

/**
 * Задание названия функции-мутации [GraphQlUpdateSublistBuilder.sublistMutationName]
 * через поле с дочерним списком [sublistFieldName].
 * Пример: для поля "changes" название функции-мутации будет "addChanges"
 */
fun <T : GraphQlUpdateSublistBuilder> T.setSublistMutationName(sublistFieldName: String): T {
    sublistMutationName = sublistFieldName
    return this
}