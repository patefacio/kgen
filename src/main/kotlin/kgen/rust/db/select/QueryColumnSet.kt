package kgen.rust.db.select

import kgen.db.DbColumn
import kgen.doubleQuote
import kgen.rust.*
import kgen.rust.db.asRustField
import kgen.rust.db.select.QueryColumn.Companion.fromDbColumn

data class QueryColumnSet(
    val nameId: String,
    val doc: String = "Support for $nameId column query fields",
    val queryColumns: List<QueryColumn>
) {
    val numColumns = queryColumns.size

    fun pullFieldsRust(fromVar: String = "row") =
        queryColumns.joinToString(";\n") { queryColumn ->
            "self.${queryColumn.id.snake} = $fromVar.${queryColumn.expression}"
        }

    val asRustStruct
        get() = Struct(
            nameId,
            doc,
            queryColumns.map { queryColumn -> queryColumn.dbColumn!!.asRustField },
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
            attrs = commonDerives + derive("Default", "Eq", "PartialEq", "Hash")
        )

    companion object {
        fun fromDbColumns(dbColumns: List<DbColumn>) = dbColumns.map { fromDbColumn(it) }
    }
}

val List<DbColumn>.asQueryColumns: List<QueryColumn> get() = QueryColumnSet.fromDbColumns(this)
