package kgen.rust.db

import kgen.asId
import kgen.db.DbTable
import kgen.rust.*
import kgen.rust.db.select.QueryColumnSet
import kgen.rust.db.select.SelectAllWhereFn
import kgen.rust.db.select.SelectAllFn
import kgen.rust.db.select.asQueryColumns

data class TableGatewayGenerator(
    val table: DbTable
) {

    val tableGateway = TableGateway(table)
    val id = tableGateway.id
    val columnCount = tableGateway.columnCount
    val pkeyStructId = "${id.snake}_pkey".asId

    val keyColumnSet = if (table.hasPrimaryKey) {
        QueryColumnSet(
            pkeyStructId.snake,
            "Primary key fields for `${id.capCamel}`",
            table.primaryKeyColumns.asQueryColumns
        )
    } else {
        null
    }

    val keyStruct = keyColumnSet?.asRustStruct

    val tableStruct = Struct(
        "table_${id.snake}",
        """Table Gateway Support for table `${id.snake}`.
            |Rows
        """.trimMargin(),
        consts = listOf(
            Const(
                "column_count",
                "The total number of key and value columns",
                USize,
                columnCount
            )
        ),
        typeImpl = TypeImpl(
            "Table${id.capCamel}".asType,
            SelectAllWhereFn(tableGateway).selectAllWhereFn,
            SelectAllFn(tableGateway).selectAllFn,
            BasicInsert(tableGateway).insertStatement,
            BulkInsert(tableGateway).bulkInsertFn,
            BulkUpsert(tableGateway).bulkUpsertFn,
            Fn(
                "delete_all",
                "Delete all rows of `${id.snake}`",
                clientFnParam,
                isAsync = true,
                hasTokioTest = true,
                inlineDecl = InlineDecl.Inline,
                returnType = "Result<u64, tokio_postgres::Error>".asType,
                returnDoc = "Number of rows deleted",
                body = FnBody("Ok(client.execute(\"DELETE FROM ${table.nameId}\", &[]).await?)"),
                hasUnitTest = false
            ),
        ),
        attrs = commonDerives + derive("Default")
    )

    val asModule = Module(
        table.nameId,
        """Table gateway pattern implemented for ${id.capCamel}""",
        uses = listOf(
            "tokio_postgres::types::{Date, FromSql, ToSql}",
            "chrono::{NaiveDate, NaiveDateTime}"
        ).asUses,
        structs = listOfNotNull(
            tableGateway.rowDataStruct, tableGateway.rowEntryStruct,
            keyStruct, tableStruct
        ),
    )

}

fun main() {

    println("TableGatewayGenerator")
}
