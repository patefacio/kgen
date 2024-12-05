package kgen.db

import kgen.asId
import org.jetbrains.exposed.sql.Table

data class DbTable(
    val nameId: String,
    val doc: String? = null,
    val columns: List<DbColumn>,
    val primaryKeyColumns: List<DbColumn>,
    val uniqueIndices: Map<String, List<DbColumn>> = emptyMap()
) {
    val id get() = nameId.asId
    val valueColumns get() = columns.filter { it !in primaryKeyColumns && !it.isAutoIncrement }
    val autoIncColumn get() = columns.firstOrNull { it.isAutoIncrement }
    val nonAutoIncColumns get() = columns.filter { it != autoIncColumn}
    val hasAutoInc: Boolean get() = autoIncColumn != null
    val hasPrimaryKey get() = primaryKeyColumns.isNotEmpty()

    init {
        println("AUTO INC ($nameId) -> ${autoIncColumn?.nameId}");
        println("PRIMARY KEY($nameId) -> ${primaryKeyColumns.map {it.nameId}}");
        println("VALUE COLUMNS($nameId) -> ${valueColumns.map {it.nameId}}");
    }


    val classifier: DbTableClassifier = when {
        hasAutoInc -> when {
            hasPrimaryKey -> DbTableClassifier.AutoIdWithPkey
            else -> DbTableClassifier.AutoId
        }

        hasPrimaryKey -> DbTableClassifier.Pkey
        else -> DbTableClassifier.Keyless
    }

    companion object {

        private fun findByName(allColumns: List<DbColumn>, names: List<String>) = names.flatMap {
            val id = it.asId
            allColumns.filter { it.id == id }
        }

        fun fromTable(table: Table, doc: String? = null): DbTable {
            val allColumns = table.columns.map { DbColumn.fromColumn(it) }
            val primaryKeyColumns = findByName(allColumns, table.primaryKey?.columns?.map { it.name } ?: emptyList())
            val uniqueIndices = table.indices.map { index ->
                index.indexName to findByName(allColumns, index.columns.map { it.name })
            }.toMap()
            return DbTable(
                table.tableName.asId.snake, doc, allColumns, primaryKeyColumns, uniqueIndices
            )
        }
    }
}

val Table.asDbTable get() = DbTable.fromTable(this)

fun Table.intoDbTable(doc: String) = DbTable.fromTable(this, doc)