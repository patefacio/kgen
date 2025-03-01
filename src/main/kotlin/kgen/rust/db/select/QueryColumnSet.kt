package kgen.rust.db.select

import kgen.db.DbColumn
import kgen.doubleQuote
import kgen.rust.*
import kgen.rust.db.asRustField
import kgen.rust.db.select.QueryColumn.Companion.fromDbColumn

/**
 * Represents a set of queryable columns in Rust, providing support for generating Rust code
 * for working with structured query results.
 *
 * This class is designed to aggregate multiple query columns, compute metadata such as the number
 * of columns, and generate Rust code for accessing and using these columns.
 *
 * @property nameId The unique identifier (name) for the query column set.
 * @property doc The documentation string associated with the query column set.
 *        Defaults to `"Support for $nameId column query fields"`.
 * @property queryColumns A list of query columns included in the set.
 */
data class QueryColumnSet(
    val nameId: String,
    val doc: String = "Support for $nameId column query fields",
    val queryColumns: List<QueryColumn>
) {

    /**
     * The number of query columns in the set.
     */
    val numColumns = queryColumns.size

    /**
     * Generates Rust code for pulling field values from a variable (e.g., a database row).
     *
     * @param fromVar The name of the variable to pull fields from. Defaults to `"row"`.
     * @return A string representing the Rust code to pull field values.
     *
     * Example output:
     * ```
     * self.field1 = row.expression1;
     * self.field2 = row.expression2;
     * ```
     */
    fun pullFieldsRust(fromVar: String = "row") =
        queryColumns.joinToString(";\n") { queryColumn ->
            "self.${queryColumn.id.snake} = $fromVar.${queryColumn.expression}"
        }

    /**
     * Generates a Rust struct representing the query column set.
     *
     * The struct includes:
     * - Fields corresponding to the query columns.
     * - A constant for the number of fields (`NUM_FIELDS`).
     * - A constant for the names of the fields (`FIELD_NAMES`).
     * - Common traits derived, such as `Default`, `Eq`, `PartialEq`, and `Hash`.
     *
     * @return A [Struct] instance representing the Rust struct.
     */
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
            attrs = commonDerives + if (queryColumns.any { it.rustType in listOf(F64, "Option<f64>".asType) }) {
                derive("Default", "PartialEq")
            } else {
                derive("Default", "Eq", "PartialEq", "Hash")
            }
        )

    companion object {
        /**
         * Creates a list of query columns from a list of database columns.
         *
         * @param dbColumns The list of database columns to convert.
         * @return A list of [QueryColumn] instances corresponding to the database columns.
         */
        fun fromDbColumns(dbColumns: List<DbColumn>) = dbColumns.map { fromDbColumn(it) }
    }
}

/**
 * Extension property to convert a list of [DbColumn] objects into a list of [QueryColumn] objects.
 */
val List<DbColumn>.asQueryColumns: List<QueryColumn> get() = QueryColumnSet.fromDbColumns(this)
