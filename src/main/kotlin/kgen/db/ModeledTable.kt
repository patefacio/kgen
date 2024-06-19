package kgen.db

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
