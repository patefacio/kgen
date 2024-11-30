package kgen.db

import kgen.asId
import org.jetbrains.exposed.sql.Column

data class DbColumn(
    val nameId: String,
    val doc: String? = null,
    val type: DbType
) {
    val id get() = nameId.asId

    val isAutoIncrement get() = type == DbType.IntegerAutoInc || type == DbType.UlongAutoInc || type == DbType.LongAutoInc

    companion object {
        private val varcharRegex = """VARCHAR\((\d+)\)""".toRegex(option = RegexOption.IGNORE_CASE)

        fun <T> getDbType(column: Column<T>): DbType {

            val sqlType = column.columnType.sqlType()
            val varCharSize = varcharRegex.find(sqlType)?.groupValues?.get(1)?.length ?: 0

            return if (varCharSize > 0) {
                DbType.VarChar(varCharSize)
            } else when (column.columnType.sqlType().uppercase()) {
                "BYTE" -> DbType.Byte
                "DOUBLE" -> DbType.Double
                "INT" -> DbType.Integer
                "SMALLINT" -> DbType.SmallInteger
                "BIGINT" -> DbType.BigInteger
                "TEXT" -> DbType.Text
                "DATE" -> DbType.Date
                "DATETIME" -> DbType.DateTime
                "TIMESTAMP" -> DbType.DateTime
                "INTERVAL" -> DbType.Interval
                "SERIAL" -> DbType.IntegerAutoInc
                "BIGSERIAL" -> DbType.LongAutoInc
                "UBIGSERIAL" -> DbType.UlongAutoInc
                "UUID" -> DbType.Uuid
                "BYTEA" -> DbType.Binary
                //"BINARYSIZED" -> DbType.Binary
                //"BLOB" -> DbType.Blob
                "JSONBINARY" -> DbType.JsonBinary
                "JSON" -> DbType.Json
                else -> DbType.Json
            }
        }

        fun <T> fromColumn(column: Column<T>) = DbColumn(column.name.asId.snake, type = getDbType(column))
    }

}