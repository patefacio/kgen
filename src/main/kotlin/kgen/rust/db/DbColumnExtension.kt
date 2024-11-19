package kgen.rust.db

import kgen.asId
import kgen.db.DbColumn
import kgen.db.DbType
import kgen.rust.Field

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
        DbType.Integer, DbType.IntegerAutoInc -> "::int"
        DbType.Double -> "::double precision"
        DbType.Byte -> "::bytea"
        DbType.BigInteger -> "::bigint"
        DbType.Date -> "::date"
        DbType.DateTime -> "::timestamp"
        DbType.SmallInteger -> "::smallint"
        DbType.Timestamp -> "::timestamptz"
        DbType.Interval -> "::interval"
        DbType.Uuid -> "::uuid"
        DbType.Text -> "::text"
        is DbType.VarChar -> "::varchar"
        else -> "::TODO"
    }

fun DbColumn.pushValue(item: String) = when (this.type) {
    is DbType.VarChar -> "${this.nameId}.push(&$item.${this.nameId});"
    DbType.Text -> "${this.nameId}.push(&$item.${this.nameId});"
    else -> "${this.nameId}.push($item.${this.nameId});"
}

