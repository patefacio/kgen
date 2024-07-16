package kgen.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

data class ModeledColumn(
    override val nameId: String,
    override val doc: String? = null,
    override val type: DbType
) : DbColumn

data class ModeledTable(
    override val nameId: String,
    override val doc: String? = null,
    override val columns: List<ModeledColumn>,
    override val primaryKeyColumnNames: Set<String> = emptySet()
) : DbTable

fun <T> Column<T>.asModeledColumn(doc: String? = null): ModeledColumn {
    println(this.columnType.sqlType())
    return ModeledColumn(
        nameId = this.name,
        doc = doc,
        type = when(this.columnType.sqlType()) {
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
            "VARCHAR" -> DbType.Json
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
        primaryKeyColumnNames = this.primaryKey?.columns?.map { it.name }?.toSet() ?: emptySet()
    )
}