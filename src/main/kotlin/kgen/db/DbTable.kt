package kgen.db

interface DbTable {
    val nameId: String
    val doc: String?
    val columns: List<DbColumn>
    val primaryKeyColumns: List<DbColumn>
    val uniqueIndices: Map<String, List<String>> get() = emptyMap()

    val valueColumns get() = columns.filter { it !in primaryKeyColumns }
    val primaryKeyColumnNameIds get() = primaryKeyColumns.map { it.nameId }

    val hasPrimaryKey get() = primaryKeyColumns.isNotEmpty()
    val hasAutoInc: Boolean get() = columns.any { it.isAutoIncrement }
}