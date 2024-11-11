package kgen.db

data class ModeledColumn(
    override val nameId: String,
    override val doc: String? = null,
    override val type: DbType
) : DbColumn