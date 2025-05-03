package kgen.rust.db

import kgen.rust.*
import kgen.rustQuote

/** Responsible for generating `bulk_insert` method for the table.
 * Intended for insertion of large sets, requires a chunk size to insert
 * chunks of rows. If the table has an _auto id_ the bulk insert consumes
 * the rows, gets the _auto id_ and wraps both in the _Entry_ struct.
 * If no _auto id_ it borrows the rows to upsert.
 *
 *  @property tableGateway Data associated with the table
 *  @property autoIdDetails Details if the table has an _auto id_
 */
data class BulkInsert(
    val tableGateway: TableGateway,
    val autoIdDetails: AutoIdDetails?
) {

    /** Table name */
    val tableName get() = tableGateway.table.tableName

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

    val insertStatement = rustQuote(
        """insert into $tableName
${tableGateway.nonAutoIncColumnSetLiteralValue}
SELECT * FROM UNNEST
${tableGateway.unnestedColumnExpressionValue}$returningId
"""
    )

    /** Input param type for _bulk insert_ - consumed vector if table has _auto id_, else slice */
    val inputFnParam = autoIdDetails?.inputFnParam ?: FnParam(
        "rows",
        "&[${tableGateway.rowDataStructName}]".asType,
        "Row data to insert"
    )

    val bulkInsertFn
        get() = Fn(
            "bulk_insert",
            "Insert large batch of [${id.capCamel}] rows.",
            clientFnParam,
            inputFnParam,
            tableGateway.bulkInsertChunkSizeFnParam,
            genericParamSet = genericClientParamSet,
            returnType = "Result<${autoIdDetails?.outputType ?: "()"}, tokio_postgres::Error>".asType,
            returnDoc = autoIdDetails?.insertReturnDoc ?: "Success or tokio_postgres::Error",
            body = FnBody(
                """
${autoIdDetails?.autoIdVecLet ?: ""}
${table.unnestColumnVectorDecls}

let insert_statement = ${tableGateway.formatStatement(insertStatement)};
for (chunk, chunk_rows) in rows.chunks(chunk_size).enumerate() {
    for row in chunk_rows.iter() {
${table.bulkUpdateUnnestAssignments}
    }
    
    let chunk_result = client.$queryOrExecute(
        &insert_statement,
        &[${table.nonAutoIncColumns.joinToString(", ") { "&${it.nameId}" }}]
    ).await;
    
    match &chunk_result {
        Err(err) => {
            tracing::error!("Failed bulk_insert `${table.nameId}` chunk({chunk}) -> {err}");
            chunk_result?;
        }
        Ok(chunk_result) => {
            tracing::debug!("Finished bulk insert of size({}) in `${table.nameId}`", $insertCount);
            ${autoIdDetails?.pushAutoId ?: ""} 
        }
    }
    ${table.bulkUnnestClearStatements}
}

Ok(${autoIdDetails?.collectResult ?: "()"})""".trimIndent()
            ),
            isAsync = true,
            hasUnitTest = false,
            testFnAttrs = attrSerializeTest.asAttrList
        )
}
