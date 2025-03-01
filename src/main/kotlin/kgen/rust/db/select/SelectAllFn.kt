package kgen.rust.db.select

import kgen.rust.*
import kgen.rust.db.TableGateway
import kgen.rust.db.clientFnParam
import kgen.rust.db.genericClientParamSet

/**
 * Generates a Rust function for executing a "SELECT * FROM ..." query without a `WHERE` clause
 * (i.e., retrieving all rows from a database table).
 *
 * This class simplifies the process of creating a `select_all` function that internally calls
 * the `select_all_where` function with a default `WHERE` condition of `1=1`, effectively selecting all rows.
 *
 * @property tableGateway The gateway object providing metadata and access to the database table.
 * @property queryColumns A list of query columns to include in the `SELECT` statement. Defaults to all columns of the table.
 */
data class SelectAllFn(
    val tableGateway: TableGateway,
    val queryColumns: List<QueryColumn> = tableGateway.table.columns.map { it.asQueryColumn },
) {

    /**
     * An instance of [SelectAllWhereFn] used to implement the `select_all` function by delegating
     * to the `select_all_where` function.
     */
    val selectAllFnImpl = SelectAllWhereFn(tableGateway, queryColumns)

    /**
     * The return type for the query results, typically a struct representing a row in the table.
     */
    val returnType = this.selectAllFnImpl.returnType

    /**
     * The generated Rust function for executing the "SELECT * FROM ..." query.
     *
     * This function is generated to retrieve all rows from the specified table. It internally
     * calls the `select_all_where` function with a default `WHERE` clause of `1=1` to include all rows.
     *
     * @return A vector containing all rows from the table.
     *
     * Example Rust function output:
     * ```
     * async fn select_all(client: &impl GenericClient) -> Vec<RowType> {
     *     Self::select_all_where(client, "1=1", &[]).await
     * }
     * ```
     */
    val selectAllFn
        get() = Fn(
            "select_all",
            "Select rows of `${this.selectAllFnImpl.id.snake}`",
            clientFnParam,
            genericParamSet = genericClientParamSet,
            isAsync = true,
            hasUnitTest = false,
            body = FnBody("""Self::select_all_where(client, "1=1", &[]).await"""),
            returnType = "Vec<$returnType>".asType,
            returnDoc = "Selected rows",
            inlineDecl = InlineDecl.Inline,
            testFnAttrs = attrSerializeTest.asAttrList
        )
}
