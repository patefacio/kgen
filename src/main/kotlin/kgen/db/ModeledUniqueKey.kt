package kgen.db

data class ModeledUniqueKey(
    val nameId: String,
    val doc: String? = null,
    val columns: List<ModeledColumn>
)
