package io.extremum.groundClient.builder.core.outputField

import de.cronn.reflection.util.TypedPropertyGetter
import io.extremum.groundClient.builder.core.Pageable
import io.extremum.groundClient.builder.core.PagingAndSortingRequest
import io.extremum.groundClient.builder.core.outputField.OutputFields.addDefaultPagingForCollections
import io.extremum.groundClient.builder.core.setPaging
import io.extremum.groundClient.builder.util.StringUtils
import io.extremum.sharedmodels.basic.BasicModel
import io.extremum.sharedmodels.basic.GraphQlList
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

object OutputFields {

    fun <T> field(property: KProperty1<T, Any?>): OutputFieldKPr<T, Any?> =
        OutputFieldKPr<T, Any?>(property)
            .addDefaultPagingForCollections(property)

    fun <T, V> field(property: KProperty1<T, Any?>, nested: List<OutputFieldKPr<V, out Any?>>): OutputFieldKPr<T, V> =
        OutputFieldKPr(property, nested)
            .addDefaultPagingForCollections(property)

    fun <T, V> field(property: KProperty1<T, Any?>, vararg nested: OutputFieldKPr<V, out Any?>): OutputFieldKPr<T, V> =
        field(property, nested.asList())

    fun <T, V> field(property: KProperty1<T, Any?>, vararg nested: KProperty1<V, Any?>): OutputFieldKPr<T, V> {
        val nestedList = nested.asList().map { field(it) }
        return field(property, nestedList)
    }

    fun field(name: String): OutputFieldStr = OutputFieldStr(name)

    fun field(name: String, nested: List<OutputFieldStr>): OutputFieldStr =
        OutputFieldStr(name, nested)

    fun field(name: String, vararg nested: OutputFieldStr): OutputFieldStr =
        field(name, nested.asList())

    fun field(name: String, vararg nested: String): OutputFieldStr {
        val nestedList: List<OutputFieldStr> = nested.asList().map { OutputFieldStr(it) }
        return field(name, nestedList)
    }

    inline fun <reified T : BasicModel<*>> field(
        name: String,
        vararg nested: TypedPropertyGetter<T, *>
    ): OutputFieldStr {
        val nestedList = nested.asList()
            .map { field(it) }
        return field(name, nestedList)
    }

    inline fun <reified T : BasicModel<*>> field(
        nameGetter: TypedPropertyGetter<T, *>,
        nested: List<OutputFieldStr>
    ): OutputFieldStr =
        OutputFieldStr(StringUtils.getFieldName(T::class, nameGetter), nested)
            .addDefaultPagingForCollections(nameGetter)

    inline fun <reified T : BasicModel<*>> field(
        nameGetter: TypedPropertyGetter<T, *>,
        vararg nested: OutputFieldStr
    ): OutputFieldStr =
        field(nameGetter, nested.asList())

    inline fun <reified T : BasicModel<*>> field(nameGetter: TypedPropertyGetter<T, *>): OutputFieldStr =
        field(StringUtils.getFieldName(T::class, nameGetter))
            .addDefaultPagingForCollections(nameGetter)

    inline fun <reified T : BasicModel<*>, reified V : BasicModel<*>> field(
        nameGetter: TypedPropertyGetter<T, V>,
        vararg nested: TypedPropertyGetter<V, *>
    ): OutputFieldStr {
        val nestedList: List<OutputFieldStr> = nested.asList()
            .map { field(it) }
        return field(nameGetter, nestedList)
    }

    /**
     * Для предотвращения ошибок для полей-списков [GraphQlList] добавляем дефолтную пагинацию
     */
    inline fun <reified T : BasicModel<*>> OutputFieldStr.addDefaultPagingForCollections(nameGetter: TypedPropertyGetter<T, *>): OutputFieldStr {
        val fieldType = StringUtils.getFieldType(T::class, nameGetter)
        return this.addDefaultPagingForCollections(fieldType)
    }

    /**
     * См. [addDefaultPagingForCollections]
     */
    private fun <T, V> OutputFieldKPr<T, V>.addDefaultPagingForCollections(property: KProperty1<T, Any?>): OutputFieldKPr<T, V> {
        val fieldType = property.returnType.jvmErasure
        return this.addDefaultPagingForCollections(fieldType.java)
    }

    fun <T : Pageable> T.addDefaultPagingForCollections(fieldType: Class<*>): T =
        if (fieldType == GraphQlList::class.java) this.setPaging(PagingAndSortingRequest()) else this
}