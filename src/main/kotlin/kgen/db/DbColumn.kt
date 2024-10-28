package kgen.db

interface DbColumn {
    val nameId: String
    val doc: String?
    val type: DbType

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