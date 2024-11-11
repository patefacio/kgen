package kgen.rust.db

import kgen.db.DbTable
import kgen.db.ModeledColumn
import kgen.db.ModeledTable

data class SelectAllFn (
    val table: ModeledTable,
    val queryColumns: List<ModeledColumn> = emptyList(),
) {


    val formattedColumnNames = table.columns.chunked(6)
        .map { chunk -> chunk.map { it.nameId }.joinToString(", ") }
        .joinToString(",\n\t")

}