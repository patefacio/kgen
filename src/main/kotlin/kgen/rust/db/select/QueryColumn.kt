package kgen.rust.db.select

import kgen.Id
import kgen.db.DbColumn
import kgen.db.DbType
import kgen.doubleQuote

sealed class QueryColumn {

    abstract val id: Id
    abstract val expression: String
    abstract val doc: String?
    abstract val type: DbType
    abstract val isAutoInc: Boolean
    abstract val dbColumn: DbColumn?

    data class Column(override val dbColumn: DbColumn) : QueryColumn() {
        override val id get() = dbColumn.id
        override val expression: String
            get() = dbColumn.id.snake
        override val doc: String?
            get() = dbColumn.doc ?: "Corresponds to column `${dbColumn.id}` of type ${dbColumn.type}"
        override val type: DbType get() = dbColumn.type
        override val isAutoInc get() = dbColumn.isAutoIncrement
    }

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
        fun fromDbColumn(column: DbColumn) = Column(column)
    }

    val rustType get() = type.asRustType
    val rustTypeName get() = rustType.asRustName
    fun fieldAccess(fromVar: String) = "${fromVar}.${id.snake}"
    fun columnReadAccess(fromVar: String, columnIndex: Int) = "${fromVar}.get($columnIndex)"
    fun readAccess(fromVar: String) = "${fromVar}.${id.snake}"

    fun asRustLiteral(value: String): String = when (this.type) {
        is DbType.Byte, is DbType.Double, is DbType.Integer, is DbType.SmallInteger, is DbType.BigInteger -> value
        is DbType.Text -> "${doubleQuote(value)}.into()"
        is DbType.Date -> "chrono::NaiveDate::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%d\").unwrap()"
        is DbType.DateTime, is DbType.Timestamp ->
            "chrono::NaiveDateTime::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%dT%H:%M\").unwrap()"

        is DbType.IntegerAutoInc -> value
        is DbType.LongAutoInc -> value
        is DbType.UlongAutoInc -> value
        is DbType.Binary, is DbType.BinarySized -> value
        is DbType.Uuid -> "uuid::Uuid::parse_str(\"$value\").unwrap()"
        is DbType.Json, is DbType.VarChar -> "${doubleQuote(value)}.into()"


        is DbType.NullableByte, is DbType.NullableDouble, is DbType.NullableInteger,
        is DbType.NullableSmallInteger, is DbType.NullableBigInteger, is DbType.NullableBinary,
        is DbType.NullableBinarySized -> "Some($value)"

        is DbType.NullableText, is DbType.NullableJson, is DbType.NullableVarChar -> "Some(${doubleQuote(value)}.into())"
        is DbType.NullableDate -> "Some(chrono::NaiveDate::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%d\").unwrap())"
        is DbType.NullableDateTime, is DbType.NullableTimestamp ->
            "Some(chrono::NaiveDateTime::parse_from_str(${doubleQuote(value)}, \"%Y-%m-%dT%H:%M\").unwrap())"

        is DbType.NullableUuid -> "Some(uuid::Uuid::parse_str(\"$value\").unwrap())"
        else -> throw (Exception("Unsupported rust type for $this"))
    }
}

val DbColumn.asQueryColumn: QueryColumn get() = QueryColumn.fromDbColumn(this)
