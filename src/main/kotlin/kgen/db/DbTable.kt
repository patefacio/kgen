package kgen.db

interface DbTable {
    val nameId: String
    val doc: String?
    val columns: List<DbColumn>
    val primaryKeyColumnNames: Set<String>
}