package kgen.rust.db.select

import kgen.rust.*
import kgen.rust.db.TableGateway
import kgen.rust.db.clientFnParam
import kgen.rust.db.formattedColumnNames
import kgen.rust.db.genericClientParamSet
import kgen.rustQuote

/**
 * Generates a Rust function for executing a "SELECT * FROM ..." query with a `WHERE` clause on a database table.
 *
 * This class supports generating a function that retrieves rows from a specified database table, applying
 * a dynamic `WHERE` clause and query parameters. It includes utilities for constructing the SQL statement,
 * formatting query results, and handling auto-incremented fields.
 *
 * @property tableGateway The gateway object providing metadata and access to the database table.
 * @property queryColumns A list of query columns to include in the `SELECT` statement. Defaults to all columns of the table.
 */
data class SelectAllWhereFn(
    val tableGateway: TableGateway,
    val queryColumns: List<QueryColumn> = tableGateway.table.columns.map { it.asQueryColumn },
) {

    /**
     * The database table associated with the query.
     */
    val table = tableGateway.table

    /**
     * The identifier for the table gateway.
     */
    val id = tableGateway.id

    /**
     * The query column for the table's auto-increment field, if it exists.
     */
    val autoIncQueryColumn = tableGateway.autoIncQueryColumn

    /**
     * The SQL `SELECT` statement template for retrieving rows from the table, with a placeholder for the `WHERE` clause.
     */
    val selectStatement
        get() = rustQuote(
            """SELECT 
${table.formattedColumnNames}
FROM ${table.tableName}
WHERE {where_clause}
            """.trimMargin()
        )

    /**
     * The return type for the query results, typically a struct representing a row in the table.
     */
    val returnType = tableGateway.rowEntryStructName ?: tableGateway.rowDataStruct.structName

    /**
     * The offset for the first data column, accounting for any auto-increment field in the table.
     */
    private val firstDataOffset = if (table.hasAutoInc) {
        1
    } else {
        0
    }

    /**
     * The field assignment for the auto-increment column, if it exists.
     */
    val autoIncFieldAssignment = if (autoIncQueryColumn != null) {
        "${autoIncQueryColumn.id.snake}: ${autoIncQueryColumn.columnReadAccess("row", 0)}"
    } else {
        null
    }

    /**
     * The field assignments for all query columns, used to construct the data structure representing a row.
     */
    val fieldAssignments = listOf(
        "${tableGateway.rowDataStruct.structName} {",
        tableGateway.dataQueryColumns.queryColumns.withIndex().joinToString(",\n") { (i, queryColumn) ->
            "${queryColumn.id.snake}: ${queryColumn.columnReadAccess("row", i + firstDataOffset)}"
        },
        "}"
    ).joinToString("\n")

    /**
     * The `push` statement for adding a row to the results vector, including handling of the auto-increment column if present.
     */
    val pushStatement = if (autoIncQueryColumn != null) {
        """results.push($returnType { $autoIncFieldAssignment, data: $fieldAssignments });"""
    } else {
        """results.push($fieldAssignments);"""
    }

    /**
     * The generated Rust function for executing the "SELECT * FROM ..." query with a `WHERE` clause.
     *
     * This function formats the `SELECT` statement, executes it on the provided database client,
     * retrieves the rows, and converts them into the appropriate data structure.
     *
     * @return A vector containing the selected rows.
     */
    val selectAllWhereFn
        get() = Fn(
            "select_all_where",
            "Select rows of `${id.snake}` with provided where clause",
            clientFnParam,
            FnParam("where_clause", "&str".asType, "The where clause (sans `where` keyword)"),
            FnParam("params", "&[&(dyn ToSql + Sync)]".asType, "Any clause parameters"),
            genericParamSet = genericClientParamSet,
            isAsync = true,
            hasUnitTest = false,
            body = FnBody(
                listOf(
                    """let statement = ${tableGateway.formatStatement(selectStatement)};
let rows = match client.query(&statement, params).await {
    Ok(stmt) => stmt,
    Err(e) => {
        panic!("Error preparing statement: {e}");
    }
};

let mut results = Vec::<${returnType}>::with_capacity(rows.len());

for row in rows {
    $pushStatement
    tracing::trace!("{:?}", results.last().unwrap());
}
results
                    """.trimMargin(),
                ).joinToString("\n")
            ),
            returnType = "Vec<$returnType>".asType,
            returnDoc = "Selected rows",
            testFnAttrs = attrSerializeTest.asAttrList
        )
}
