package kgen.rust.db

import kgen.rust.*
import kgen.rustQuote

/** Responsible for generating `basic_insert` method for the table.
 * The method builds the insert statement by creating a string of
 * parameters for each field in the table for each row passed in. For smaller inserts
 * that don't need the overhead of the bulk insert.
 *
 * @property tableGateway The table gateway
 * @property autoIdDetails Details specific to tables with auto-id
 */
data class BasicInsert(
    val tableGateway: TableGateway,
    val autoIdDetails: AutoIdDetails?,
) {

    /** Table name */
    val tableName get() = tableGateway.table.tableName

    /** Underlying table */
    val table = tableGateway.table

    /** Run insert as a _query_ or as statement _execution_.
     * If `auto id` style the id's need to be returned, so use execute. If
     * simply a basic insert, use query and get return number inserted.
     */
    val queryOrExecute: String

    /** Expression for number of rows returned */
    val insertCount: String

    /** The returning clause, required to get _auto id_ back if auto id */
    val returningId: String

    /** Input param type for _bulk insert_ - consumed vector if table has _auto id_, else slice */
    val inputFnParam = autoIdDetails?.inputFnParam ?: FnParam(
        "rows",
        "&[${tableGateway.rowDataStructName}]".asType,
        "Row data to insert"
    )

    init {
        if (autoIdDetails == null) {
            queryOrExecute = "execute"
            insertCount = "insert_result"
            returningId = ""
        } else {
            queryOrExecute = "query"
            insertCount = "insert_result.len()"
            returningId = "\nreturning ${tableGateway.autoIncQueryColumn!!.id}"
        }
    }

    /** The sql insert statement */
    val basicInsertStatement = rustQuote(
        """insert into $tableName 
${tableGateway.nonAutoIncColumnSetLiteralValue}
VALUES
{value_params}$returningId
"""
    )

    /** The body of the insert function */
    val insertBody = """
        use itertools::Itertools;
        let mut param_id = 0;
        let mut params: Vec<&(dyn ToSql + Sync)> = Vec::with_capacity(rows.len() * ${tableGateway.rowDataStructName}::NUM_FIELDS);;
        let value_params = rows
            .iter()
            .map(|row| {
                let row_params = ${tableGateway.rowDataStructName}::FIELD_NAMES.map(|_| {
                    param_id = param_id + 1;
                    format!("${'$'}{param_id}")
                }).join(", ");
                
${
        tableGateway.dataQueryColumns.queryColumns.joinToString("\n") {
            "params.push(&row.${it.id});"
        }
    }                
                
                format!("({row_params})")
            }).join(",\n");

        
        let insert_result = client.$queryOrExecute(&${tableGateway.formatStatement(basicInsertStatement)}, &params).await;
        
        match insert_result {
            Err(err) => {
                tracing::error!("Failed basic_insert `${table.nameId}`");
                Err(err)
            }
            Ok(insert_result) => {
                tracing::debug!("Finished basic insert of count({}) in `${table.nameId}`", $insertCount);
                Ok(${autoIdDetails?.collectAutoId ?: "insert_result"}) 
            }
        }
        """.trimIndent()


    val basicInsertFn
        get() =
            Fn(
                "basic_insert",
                """Insert rows of `$tableName` by building parameterized statement.
                |For large insertions prefer [bulk_insert]
            """.trimMargin(),
                clientFnParam,
                inputFnParam,
                genericParamSet = genericClientParamSet,
                returnType = "Result<${autoIdDetails?.outputType ?: "u64"}, tokio_postgres::Error>".asType,
                returnDoc = autoIdDetails?.insertReturnDoc ?: "Success or tokio_postgres::Error",
                isAsync = true,
                hasUnitTest = false,
                body = FnBody(insertBody),
                testFnAttrs = attrSerializeTest.asAttrList
            )

}
