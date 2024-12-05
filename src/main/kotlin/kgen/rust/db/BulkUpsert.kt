package kgen.rust.db

import kgen.asId
import kgen.rust.*
import kgen.rustQuote

/** Responsible for generating `bulk_upsert` method for the table.
 * Intended for upserting of large sets, requires a chunk size to upsert
 * chunks of rows. If the table has an _auto id_ the bulk upsert consumes
 * the rows, gets the _auto id_ and wraps both in the _Entry_ struct.
 * If no _auto id_ it borrows the rows to upsert.
 *
 *  @property tableGateway Data associated with the table
 *  @property autoIdDetails Details if the table has an _auto id_
 */
data class BulkUpsert(
    val tableGateway: TableGateway,
    val autoIdDetails: AutoIdDetails?
) {

    /** Table id */
    val id get() = tableGateway.id

    /** Underlying table */
    val table get() = tableGateway.table

    /** Run insert as a _query_ or as statement _execution_.
     * If `auto id` style the id's need to be returned, so use execute. If
     * simply a basic insert, use query and get return number inserted.
     */
    val queryOrExecute: String

    /** Expression for number of rows returned */
    val insertCount: String

    /** The returning clause, required to get _auto id_ back if auto id */
    val returningId: String

    init {
        if (autoIdDetails == null) {
            queryOrExecute = "execute"
            insertCount = "chunk_result"
            returningId = ""
        } else {
            queryOrExecute = "query"
            insertCount = "chunk_result.len()"
            returningId = "\nreturning ${tableGateway.autoIncQueryColumn!!.id}"
        }
    }

    /** The sql `ON CONFLICT` clause indicating how to match rows if already present */
    val onConflictAssignments
        get() = table.valueColumns.joinToString(",\n\t") { column ->
            val term = column.nameId.asId
            "${term.snake} = EXCLUDED.${term.snake}"
        }

    val upsertStatement = rustQuote(
        """insert into $id
${tableGateway.nonAutoIncColumnSetLiteralValue}
SELECT * FROM UNNEST
${tableGateway.unnestedColumnExpressionValue}
ON CONFLICT (${table.onConflictKey})
DO UPDATE SET
    ${onConflictAssignments}$returningId
"""
    )

    /** Input param type for _bulk insert_ - consumed vector if table has _auto id_, else slice */
    val inputFnParam = autoIdDetails?.inputFnParam ?: FnParam(
        "rows",
        "&[${tableGateway.rowDataStructName}]".asType,
        "Row data to insert"
    )

    val bulkUpsertFn = Fn(
        "bulk_upsert",
        "Upsert large batch of [${id.capCamel}] rows.",
        clientFnParam,
        inputFnParam,
        tableGateway.bulkInsertChunkSizeFnParam,
        returnType = "Result<${autoIdDetails?.outputType ?: "()"}, tokio_postgres::Error>".asType,
        returnDoc = "",
        body = FnBody(
            """
let mut chunk = 0;${autoIdDetails?.autoIdVecLet ?: ""}
${table.unnestColumnVectorDecls}
let upsert_statement = ${tableGateway.formatStatement(upsertStatement)};
for chunk_rows in rows.chunks(chunk_size) {
    for row in chunk_rows.into_iter() {
${table.bulkUpdateUnnestAssignments}
    }
    let chunk_result = client.$queryOrExecute(
        &upsert_statement,
        &[${table.nonAutoIncColumns.joinToString(", ") { "&${it.nameId}" }}]
    ).await;
    
    match &chunk_result {
        Err(err) => {
            tracing::error!("Failed bulk_insert `${table.nameId}` chunk({chunk}) -> {err}");
            chunk_result?;
        }
        Ok(chunk_result) => {
            tracing::debug!("Finished bulk upsert of size({}) in `${table.nameId}`", $insertCount);
            ${autoIdDetails?.pushAutoId ?: ""} 
        }
    }
    chunk += 1;
    ${table.bulkUnnestClearStatements}
}
Ok(${autoIdDetails?.collectResult ?: "()"})""".trimIndent()
        ),
        isAsync = true,
        hasUnitTest = false,
        testFnAttrs = attrSerializeTest.asAttrList
    )
}
