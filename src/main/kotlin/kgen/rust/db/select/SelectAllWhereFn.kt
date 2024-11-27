package kgen.rust.db.select

import kgen.rust.*
import kgen.rust.db.TableGateway
import kgen.rust.db.clientFnParam
import kgen.rustQuote

data class SelectAllWhereFn(
    val tableGateway: TableGateway,
    val queryColumns: List<QueryColumn> = tableGateway.table.columns.map { it.asQueryColumn },
) {

    val table = tableGateway.table
    val id = tableGateway.id
    val autoIncQueryColumn = tableGateway.autoIncQueryColumn

    val formattedColumnNames = table.columns.chunked(6)
        .map { chunk -> chunk.map { it.nameId }.joinToString(", ") }
        .joinToString(",\n\t")

    val selectStatement
        get() = rustQuote(
            """SELECT 
$formattedColumnNames
FROM ${table.nameId}
WHERE {where_clause}
        """.trimMargin()
        )

    val returnType = tableGateway.rowEntryStructName ?: tableGateway.rowDataStruct.structName

    private val firstDataOffset = if (table.hasAutoInc) {
        1
    } else {
        0
    }

    val autoIncFieldAssignment = if (autoIncQueryColumn != null) {
        "${autoIncQueryColumn.id.snake}: ${autoIncQueryColumn.columnReadAccess("row", 0)}"
    } else {
        null
    }

    val fieldAssignments = listOf(
        "${tableGateway.rowDataStruct.structName} {",
        tableGateway.dataQueryColumns.queryColumns.withIndex().joinToString(",\n") { (i, queryColumn) ->
            "${queryColumn.id.snake}: ${queryColumn.columnReadAccess("row", i + firstDataOffset)}"
        },
        "}"
    ).joinToString("\n")

    val pushStatement = if(autoIncQueryColumn != null) {
        """results.push($returnType { $autoIncFieldAssignment, data: $fieldAssignments });"""
    } else {
        """results.push($fieldAssignments);"""
    }

    val selectAllWhereFn
        get() = Fn(
            "select_all_where",
            "Select rows of `${id.snake}` with provided where clause",
            clientFnParam,
            FnParam("where_clause", "&str".asType, "The where clause (sans `where` keyword)"),
            FnParam("params", "&[&(dyn ToSql + Sync)]".asType, "Any clause parameters"),
            FnParam("capacity", USize, "Capacity to the results"),
            isAsync = true,
            hasUnitTest = false,
            body = FnBody(
                listOf(
                    """let statement = format!($selectStatement);
let mut results = Vec::<${returnType}>::with_capacity(capacity);
let rows = match client.query(&statement, params).await {
    Ok(stmt) => stmt,
    Err(e) => {
        panic!("Error preparing statement: {e}");
    }
};

for row in rows {
    $pushStatement
    tracing::info!("{:?}", results.last().unwrap());
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