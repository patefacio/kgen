package kgen.rust.db

import kgen.doubleQuote
import kgen.rust.Fn
import kgen.rust.FnBody
import kgen.rust.InlineDecl
import kgen.rust.asType

/** Responsible for generating `delete` method for the table.
 * @property tableGateway The table gateway
 */
data class DeleteStatement(
    val tableGateway: TableGateway,
) {

    /** Underlying table */
    val table = tableGateway.table

    /** Id for the table */
    val tableId = table.id

    val rustDeleteStatement = RustSqlStatement(
        "delete_statement",
         doubleQuote("DELETE FROM $tableId"),
        tableGateway.backdoorTableId
    )

    val deleteAllFn
        get() =
            Fn(
                "delete_all",
                "Delete all rows of `$tableId`",
                clientFnParam,
                genericParamSet = genericClientParamSet,
                isAsync = true,
                hasTokioTest = true,
                inlineDecl = InlineDecl.Inline,
                returnType = "Result<u64, tokio_postgres::Error>".asType,
                returnDoc = "Number of rows deleted",
                body = FnBody(
                    """${rustDeleteStatement.letStatement}
                    client.execute(${rustDeleteStatement.asStr}, &[]).await""".trimIndent()
                ),
                hasUnitTest = false
            )
}