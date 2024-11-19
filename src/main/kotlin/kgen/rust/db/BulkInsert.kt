package kgen.rust.db

import kgen.db.DbTable
import kgen.rust.*
import kgen.rustQuote

data class BulkInsert(
    val tableGateway: TableGateway,
) {

    val id get() = tableGateway.id
    val table get() = tableGateway.table
    val insertStatement = rustQuote(
            """insert into ${table.nameId}
${tableGateway.nonAutoIncColumnSetLiteralValue}
SELECT * FROM UNNEST
${tableGateway.unnestedColumnExpressionValue}
"""
        )

    val bulkInsertFn get() = Fn(
        "bulk_insert",
        "Insert large batch of [${id.capCamel}] rows.",
        clientFnParam,
        tableGateway.rowsParam,
        tableGateway.bulkInsertChunkSizeFnParam,
        returnType = "Result<(), tokio_postgres::Error>".asType,
        body = FnBody(
            """
let mut chunk = 0;
${table.unnestColumnVectorDecls}
for chunk_rows in rows.chunks(chunk_size) {
    for row in chunk_rows.into_iter() {
${table.bulkUpdateUnnestAssignments}
    }
    let chunk_result = client.execute(
        $insertStatement,
        &[${table.nonAutoIncColumns.joinToString(", ") { "&${it.nameId}" }}]
    ).await;
    
    match &chunk_result {
        Err(err) => {
            tracing::error!("Failed bulk_insert `${table.nameId}` chunk({chunk}) -> {err}");
            chunk_result?;
        }
        _ => tracing::debug!("Finished inserting chunk({chunk}), size({}) in `${table.nameId}`", chunk_rows.len())
    }
    chunk += 1;
    ${table.bulkUnnestClearStatements}
}
Ok(())
        """.trimIndent()
        ),
        isAsync = true,
        hasTokioTest = true,
        testFnAttrs = attrSerializeTest.asAttrList
    )
}
