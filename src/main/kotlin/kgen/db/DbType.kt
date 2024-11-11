package kgen.db

import kgen.rust.*
import kgen.rust.db.asRustType

sealed class DbType {
    data object Byte : DbType()
    data object Double : DbType()

    data object Integer : DbType()

    data object SmallInteger : DbType()

    data object BigInteger : DbType()

    data object Text : DbType()

    data object Date : DbType()

    data object DateTime : DbType()

    data object Timestamp : DbType()

    data object Interval : DbType()

    data object IntegerAutoInc : DbType()

    data object LongAutoInc : DbType()

    data object UlongAutoInc : DbType()

    data object Uuid : DbType()

    data object Binary : DbType()

    data class BinarySized(val length: Int) : DbType()

    data object Blob : DbType()

    data object JsonBinary : DbType()

    data object Json : DbType()

    data class VarChar(val length: Int) : DbType();

    val asRustType
        get() = when (this) {
            is DbType.Byte -> U8
            is DbType.Double -> F64
            is DbType.Integer -> I32
            is DbType.SmallInteger -> I16
            is DbType.BigInteger -> I64
            is DbType.Text -> RustString
            is DbType.Date -> "chrono::NaiveDate".asType
            is DbType.DateTime, is DbType.Timestamp -> "chrono::NaiveDateTime".asType
            is DbType.IntegerAutoInc -> I32
            is DbType.LongAutoInc -> I64
            is DbType.UlongAutoInc -> U64
            is DbType.Binary, is DbType.BinarySized -> I64
            is DbType.Uuid -> "uuid::Uuid".asType
            is DbType.Json, is DbType.VarChar -> RustString
            else -> throw (Exception("Unsupported rust type for $this"))
        }
}