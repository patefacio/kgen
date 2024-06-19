package kgen.db

interface DbColumn {
    val nameId: String
    val doc: String?
    val type: DbType
}