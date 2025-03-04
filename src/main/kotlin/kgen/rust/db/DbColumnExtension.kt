package kgen.rust.db

import kgen.asId
import kgen.db.DbColumn
import kgen.db.DbType
import kgen.doubleQuote
import kgen.rust.Field

/** Convert [DbColumn] into an sql literal, double quoting if name is mixed
 * case.
 */
val DbColumn.asSqlLiteral
    get() = if (columnName.any { it.isUpperCase() }) {
        doubleQuote(columnName)
    } else {
        columnName
    }

val DbColumn.asRustType
    get() = type.asRustType

val DbColumn.asRustField
    get() = Field(
        this.nameId.asId.snake,
        doc = this.doc ?: "Field for column `$nameId`",
        this.asRustType
    )

val DbColumn.unnestCast
    get() = when (type) {
        DbType.Integer, DbType.NullableInteger, DbType.IntegerAutoInc -> "::int"
        DbType.Double, DbType.NullableDouble -> "::double precision"
        DbType.Bool, DbType.NullableBool -> "::boolean"
        DbType.Byte, DbType.NullableByte -> "::bytea"
        DbType.BigInteger, DbType.NullableBigInteger -> "::bigint"
        DbType.Json, DbType.NullableJson -> "::json"
        DbType.JsonBinary, DbType.NullableJsonBinary -> "::jsonb"
        DbType.Date, DbType.NullableDate -> "::date"
        DbType.DateTime, DbType.NullableDateTime -> "::timestamp"
        DbType.SmallInteger, DbType.NullableSmallInteger -> "::smallint"
        DbType.Timestamp, DbType.Timestamp -> "::timestamptz"
        DbType.Interval, DbType.NullableInterval -> "::interval"
        DbType.Uuid, DbType.NullableUuid -> "::uuid"
        DbType.Text, DbType.NullableText -> "::text"
        is DbType.VarChar, is DbType.NullableVarChar -> "::varchar"
        else -> "::TODO"
    }

fun DbColumn.pushValue(item: String) = when (this.type) {
    is DbType.VarChar, is DbType.JsonBinary, is DbType.Json,
    is DbType.NullableJsonBinary, is DbType.NullableJson,
    is DbType.NullableText, is DbType.NullableVarChar -> "${this.nameId}.push(&$item.${this.nameId});"

    DbType.Text -> "${this.nameId}.push(&$item.${this.nameId});"
    else -> "${this.nameId}.push($item.${this.nameId});"
}

