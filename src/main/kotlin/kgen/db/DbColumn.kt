package kgen.db

import kgen.Id
import kgen.asId

interface DbColumn {
    val nameId: String
    val doc: String?
    val type: DbType

    val id get() = nameId.asId

    val isAutoIncrement get() = type == DbType.IntegerAutoInc || type == DbType.UlongAutoInc || type == DbType.LongAutoInc

    val unnestCast get() = when(type) {
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
}