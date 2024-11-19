package kgen.rust.db

import kgen.asId
import kgen.db.DbTable
import kgen.rust.*
import kgen.rust.db.select.QueryColumnSet
import kgen.rust.db.select.asQueryColumn

data class TableGateway(
    val table: DbTable,
) {
    val id = table.nameId.asId
    val classifier = table.classifier

    val columnCount = table.columns.size
    val rowDataIdName = "${id.snake}_data".asId
    val allQueryColumns = table.columns.map { it.asQueryColumn }
    val dataQueryColumns = allQueryColumns.filter { !it.isAutoInc }

    val rowDataColumnSet = QueryColumnSet("${id}_data", "Primary data fields", dataQueryColumns)
    val rowDataStruct = rowDataColumnSet.asRustStruct

    val autoIncQueryColumn = allQueryColumns.firstOrNull { it.isAutoInc }

    val nonAutoIncColumnSetLiteralValue = "(\n\t${table.nonAutoIncGroupedColumnNames}\n)"
    val bulkInsertChunkSizeFnParam = FnParam("chunk_size", USize, "How to chunk the inserts")
    val rowsParam = FnParam("rows", "&[${rowDataStruct.structName}]".asType, "Rows to insert")
    val unnestedColumnExpressionValue = "(\n\t${table.unnestedColumnExpressions}\n)"

    val rowEntryStruct = if (autoIncQueryColumn != null) {
        Struct(
            "${id}_entry",
            """All fields plus auto id for table `${table.id}`.""",
            autoIncQueryColumn.dbColumn!!.asRustField,
            Field("data", "The data fields", rowDataIdName.capCamel.asType),
            attrs = commonDerives
        )
    } else null

    val rowEntryStructRustName = rowEntryStruct?.asRustName
}

