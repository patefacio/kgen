package kgen.rust.db

import kgen.asId
import kgen.rust.*
import kgen.rustQuote

data class BulkUpsert(
    val tableGateway: TableGateway
) {

    val id get() = tableGateway.id
    val table get() = tableGateway.table

    val onConflictAssignments
        get() = table.valueColumns.joinToString(",\n\t") { column ->
            val term = column.nameId.asId
            "${term.snake} = EXCLUDED.${term.snake}"
        }

    val upsertStatement = rustQuote(
        """insert into ${table.nameId}
${tableGateway.nonAutoIncColumnSetLiteralValue}
SELECT * FROM UNNEST
${tableGateway.unnestedColumnExpressionValue}
ON CONFLICT (${table.pkeyAsSql})
DO UPDATE SET
    ${onConflictAssignments}
"""
    )

    val bulkUpsertFn = Fn(
        "bulk_upsert",
        "Upsert large batch of [${id.capCamel}] rows.",
        clientFnParam,
        tableGateway.rowsParam,
        tableGateway.bulkInsertChunkSizeFnParam,
        returnType = "Result<(), tokio_postgres::Error>".asType,
        returnDoc = "",
        body = FnBody(
            """
let mut chunk = 0;
${table.unnestColumnVectorDecls}
for chunk_rows in rows.chunks(chunk_size) {
    for row in chunk_rows.into_iter() {
${table.bulkUpdateUnnestAssignments}
    }
    let chunk_result = client.execute(
        $upsertStatement,
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
