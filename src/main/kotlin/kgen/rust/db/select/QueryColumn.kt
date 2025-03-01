package kgen.rust.db.select

import kgen.Id
import kgen.db.DbColumn
import kgen.db.DbType
import kgen.doubleQuote

/**
 * Represents a queryable column in Rust, with support for database column mapping,
 * custom expressions, and type transformations.
 *
 * This sealed class has two concrete implementations:
 * - [Column]: Represents a direct mapping to a database column.
 * - [Expression]: Represents a custom expression for use in queries.
 */
sealed class QueryColumn {

    /**
     * The unique identifier for the column or expression.
     */
    abstract val id: Id

    /**
     * The SQL expression associated with this column or expression.
     */
    abstract val expression: String

    /**
     * Optional documentation for the column or expression.
     */
    abstract val doc: String?

    /**
     * The database type associated with this column or expression.
     */
    abstract val type: DbType

    /**
     * Indicates whether this column is an auto-incrementing column.
     */
    abstract val isAutoInc: Boolean

    /**
     * The underlying database column, if applicable. May be `null` for expressions.
     */
    abstract val dbColumn: DbColumn?

    /**
     * Represents a direct mapping to a database column.
     *
     * @property dbColumn The database column that this query column maps to.
     */
    data class Column(override val dbColumn: DbColumn) : QueryColumn() {
        override val id get() = dbColumn.id
        override val expression: String
            get() = dbColumn.id.snake
        override val doc: String?
            get() = dbColumn.doc ?: "Corresponds to column `${dbColumn.id}` of type ${dbColumn.type}"
        override val type: DbType get() = dbColumn.type
        override val isAutoInc get() = dbColumn.isAutoIncrement
    }

    /**
     * Represents a custom SQL expression used in queries.
     *
     * @property dbColumn The underlying database column, if applicable. May be `null`.
     * @property expression The SQL expression for this query column.
     * @property type The database type associated with the expression.
     * @property id The unique identifier for the expression.
     * @property doc Optional documentation for the expression.
     */
    data class Expression(
        override val dbColumn: DbColumn? = null,
        override val expression: String,
        override val type: DbType,
        override val id: Id,
        override val doc: String? = null,
    ) : QueryColumn() {
        override val isAutoInc get() = false
    }

    companion object {
        /**
         * Creates a [Column] instance from a database column.
         *
         * @param column The database column to be converted into a query column.
         * @return A [QueryColumn.Column] instance.
         */
        fun fromDbColumn(column: DbColumn) = Column(column)
    }

    /**
     * Returns the Rust type associated with this query column.
     */
    val rustType get() = type.asRustType

    /**
     * Returns the name of the Rust type associated with this query column.
     */
    val rustTypeName get() = rustType.asRustName

    /**
     * Generates Rust field access code for this query column.
     *
     * @param fromVar The variable from which the field is accessed.
     * @return The Rust code for accessing the field.
     */
    fun fieldAccess(fromVar: String) = "${fromVar}.${id.snake}"

    /**
     * Generates Rust code for reading the column value from a query result row.
     *
     * @param fromVar The variable representing the query result row.
     * @param columnIndex The index of the column in the row.
     * @return The Rust code for accessing the column value.
     */
    fun columnReadAccess(fromVar: String, columnIndex: Int) = "${fromVar}.get($columnIndex)"

    /**
     * Generates Rust code for reading the value of this query column.
     *
     * @param fromVar The variable from which the column value is accessed.
     * @return The Rust code for accessing the column value.
     */
    fun readAccess(fromVar: String) = "${fromVar}.${id.snake}"

    /**
     * Generates a Rust literal representation of the given value for this column's type.
     *
     * @param value The value to be represented as a Rust literal.
     * @return The Rust literal for the value, formatted based on the column's type.
     * @throws Exception If the type is unsupported for Rust literal representation.
     */
    fun asRustLiteral(value: String): String = when (this.type) {
        is DbType.Byte, is DbType.Double, is DbType.Integer, is DbType.SmallInteger, is DbType.BigInteger -> value
        is DbType.Text -> "${doubleQuote(value)}.into()"
        is DbType.Date -> "chrono::NaiveDate::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%d\").unwrap()"
        is DbType.DateTime, is DbType.Timestamp ->
            "chrono::NaiveDateTime::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%dT%H:%M\").unwrap()"

        is DbType.IntegerAutoInc, is DbType.LongAutoInc, is DbType.UlongAutoInc -> value
        is DbType.Binary, is DbType.BinarySized -> value
        is DbType.Bool -> value
        is DbType.Uuid -> "uuid::Uuid::parse_str(\"$value\").unwrap()"
        is DbType.Json, is DbType.VarChar -> "${doubleQuote(value)}.into()"
        is DbType.NullableByte, is DbType.NullableDouble, is DbType.NullableInteger,
        is DbType.NullableSmallInteger, is DbType.NullableBigInteger, is DbType.NullableBinary,
        is DbType.NullableBinarySized -> "Some($value)"

        is DbType.NullableBool -> "Some($value)"
        is DbType.NullableText, is DbType.NullableJson, is DbType.NullableVarChar ->
            "Some(${doubleQuote(value)}.into())"

        is DbType.NullableDate ->
            "Some(chrono::NaiveDate::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%d\").unwrap())"

        is DbType.NullableDateTime, is DbType.NullableTimestamp ->
            "Some(chrono::NaiveDateTime::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%dT%H:%M\").unwrap())"

        is DbType.NullableUuid -> "Some(uuid::Uuid::parse_str(\"$value\").unwrap())"
        else -> throw (Exception("Unsupported rust type for $this"))
    }
}

/**
 * Extension property to convert a [DbColumn] to a [QueryColumn].
 */
val DbColumn.asQueryColumn: QueryColumn get() = QueryColumn.fromDbColumn(this)
