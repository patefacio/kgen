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
    return ModeledColumn(
        nameId = this.name,
        doc = doc,
        type = DbType.Json
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