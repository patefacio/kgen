package kgen.rust.db

import kgen.db.DbColumn
import kgen.doubleQuote
import kgen.rust.*
import kgen.rust.db.QueryColumn.Companion.fromDbColumn

data class QueryColumnSet(
    val nameId: String,
    val doc: String = "Support for $nameId column query fields",
    val queryColumns: List<QueryColumn>
) {
    val numColumns = queryColumns.size
    val asRustStruct
        get() = Struct(
            nameId,
            doc,
            queryColumns.withIndex().map { indexedValue ->
                val index = indexedValue.index
                val queryColumn = indexedValue.value
                val fieldName = queryColumn.id?.snake ?: "c_$index"
                Field(fieldName, doc, queryColumn.type.asRustType)
            },
            consts = listOf(
                Const("num_fields", "Number of fields", USize, value = numColumns),
                Const(
                    "field_names",
                    "Names of fields", "[& 'static str; Self::NUM_FIELDS]".asType,
                    value = listOf(
                        "[",
                        queryColumns.joinToString(", ") { doubleQuote(it.id.snake) },
                        "]"
                    )
                        .joinToString("\n")
                        .asConstValue
                )
            ),
            attrs = commonDerives + derive("Default")
        )

    companion object {
        fun fromDbColumns(dbColumns: List<DbColumn>) = dbColumns.map { fromDbColumn(it) }
    }
}

val List<DbColumn>.asQueryColumns: List<QueryColumn> get() = QueryColumnSet.fromDbColumns(this)
