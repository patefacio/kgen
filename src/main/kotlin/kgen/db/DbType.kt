package kgen.db

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

   data class VarChar(val length: Int) : DbType()

}