package kgen.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

data class ModeledTable(
    override val nameId: String,
    override val doc: String? = null,
    override val columns: List<ModeledColumn>,
    override val primaryKeyColumns: List<ModeledColumn>,
    val modeledUniqueKeys: List<ModeledUniqueKey> = emptyList(),
    override val uniqueIndices: Map<String, List<String>> = modeledUniqueKeys.associate { it.nameId to it.columns.map { it.nameId } },
) : DbTable {
}

private val varcharRegex = """VARCHAR\((\d+)\)""".toRegex(option = RegexOption.IGNORE_CASE)

fun <T> Column<T>.asModeledColumn(doc: String? = null): ModeledColumn {
    val sqlType = this.columnType.sqlType()
    val varCharSize = varcharRegex.find(sqlType)?.groupValues?.get(1)?.length ?: 0

    return ModeledColumn(
        nameId = this.name,
        doc = doc,
        type = if (varCharSize > 0) {
            DbType.VarChar(varCharSize)
        } else when (this.columnType.sqlType()) {
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
            "uuid" -> DbType.Uuid
            "bytea" -> DbType.Binary
            //"BINARYSIZED" -> DbType.Binary
            //"BLOB" -> DbType.Blob
            "JSONBINARY" -> DbType.JsonBinary
            "JSON" -> DbType.Json
            else -> DbType.Json
        }
    )
}

fun Table.asModeledTable(doc: String? = null): ModeledTable {
    return ModeledTable(
        nameId = this.tableName,
        doc = doc,
        columns = this.columns.map {
            it.asModeledColumn()
        },
        primaryKeyColumns = this.primaryKey?.columns?.map { it.asModeledColumn() } ?: emptyList(),
    )
}