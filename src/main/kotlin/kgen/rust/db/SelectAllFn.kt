package kgen.rust.db

import kgen.db.DbTable

data class SelectAllFn (
    val table: DbTable,
    val queryColumns: List<QueryColumn> = emptyList(),
) {


}