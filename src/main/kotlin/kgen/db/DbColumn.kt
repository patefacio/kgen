package kgen.db

import kgen.asId
import org.jetbrains.exposed.sql.Column

/** Models a database column
 * @property nameId Snake case name for the column
 * @property doc Comment for the sql column
 * @property type The column type
 * @property columnName The name of the column, defaulted to [nameId]
 */
data class DbColumn(
    val nameId: String,
    val doc: String? = null,
    val type: DbType,
    val columnName: String = nameId
) {
    val id get() = nameId.asId

    val isAutoIncrement get() = type == DbType.IntegerAutoInc || type == DbType.UlongAutoInc || type == DbType.LongAutoInc

    companion object {
        private val varcharRegex = """VARCHAR\((\d+)\)""".toRegex(option = RegexOption.IGNORE_CASE)

        fun <T> getDbType(column: Column<T>): DbType {

            val sqlType = column.columnType.sqlType()
            val isNullable = column.columnType.nullable
            val varCharSize = varcharRegex.find(sqlType)?.groupValues?.get(1)?.length ?: 0

            return if (varCharSize > 0) {
                DbType.VarChar(varCharSize)
            } else when (column.columnType.sqlType().uppercase()) {
                "BYTE" -> if (isNullable) DbType.NullableByte else DbType.Byte
                "DOUBLE", "DOUBLE PRECISION" -> if (isNullable) DbType.NullableDouble else DbType.Double
                "INT" -> if (isNullable) DbType.NullableInteger else DbType.Integer
                "SMALLINT" -> if (isNullable) DbType.NullableSmallInteger else DbType.SmallInteger
                "BIGINT" -> if (isNullable) DbType.NullableBigInteger else DbType.BigInteger
                "TEXT" -> if (isNullable) DbType.NullableText else DbType.Text
                "DATE" -> if (isNullable) DbType.NullableDate else DbType.Date
                "DATETIME", "TIMESTAMP" -> if (isNullable) DbType.NullableDateTime else DbType.DateTime
                "INTERVAL" -> if (isNullable) DbType.NullableInterval else DbType.Interval
                "SERIAL" -> DbType.IntegerAutoInc
                "BIGSERIAL" -> DbType.LongAutoInc
                "UBIGSERIAL" -> DbType.UlongAutoInc
                "UUID" -> if (isNullable) DbType.NullableUuid else DbType.Uuid
                "BYTEA" -> if (isNullable) DbType.NullableBinary else DbType.Binary
                "BOOLEAN" -> if (isNullable) DbType.NullableBool else DbType.Bool
                //"BINARYSIZED" -> DbType.Binary
                //"BLOB" -> DbType.Blob
                "JSONBINARY" -> if (isNullable) DbType.NullableJsonBinary else DbType.JsonBinary
                "JSON" -> if (isNullable) DbType.NullableJson else DbType.Json
                else -> DbType.Json
            }
        }

        /** Convert from exposed column to [DbColumn] */
        fun <T> fromColumn(column: Column<T>) = DbColumn(column.name.asId.snake, type = getDbType(column))
    }

}