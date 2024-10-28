package kgen.db

interface DbTable {
    val nameId: String
    val doc: String?
    val columns: List<DbColumn>
    val primaryKeyColumns: List<DbColumn>

    val valueColumns get() = columns.filter { it !in primaryKeyColumns }
    val primaryKeyColumnNameIds get() = primaryKeyColumns.map { it.nameId }
}