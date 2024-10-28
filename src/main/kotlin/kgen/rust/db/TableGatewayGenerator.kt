package kgen.rust.db

import kgen.asId
import kgen.db.DbColumn
import kgen.db.DbTable
import kgen.db.DbType
import kgen.indent
import kgen.rust.*
import kgen.rustQuote

val DbColumn.asRustType
    get() = when (this.type) {

        is DbType.Byte -> U8
        is DbType.Double -> F64
        is DbType.Integer -> I32
        is DbType.SmallInteger -> I16
        is DbType.BigInteger -> I64
        is DbType.Text -> RustString
        is DbType.Date -> "chrono::NaiveDate".asType
        is DbType.DateTime, is DbType.Timestamp -> "chrono::NaiveDateTime".asType
        is DbType.IntegerAutoInc -> I32
        is DbType.LongAutoInc -> I64
        is DbType.UlongAutoInc -> U64
        is DbType.Binary, is DbType.BinarySized -> I64
        is DbType.Uuid -> "uuid::Uuid".asType
        is DbType.Json, is DbType.VarChar -> RustString

        else -> throw (Exception("Unsupported rust type for $this"))
    }

val DbColumn.asRustField
    get() = Field(
        this.nameId.asId.snake,
        doc = this.doc ?: "Field for column `$nameId`",
        this.asRustType
    )

fun DbColumn.pushValue(item: String) = when(this.type) {
    is DbType.VarChar -> "${this.nameId}.push(&$item.${this.nameId});"
    DbType.Text -> "${this.nameId}.push(&$item.${this.nameId});"
    else -> "${this.nameId}.push($item.${this.nameId});"
}

data class TableGatewayGenerator(
    val table: DbTable
) {

    val id = table.nameId.asId
    val columnCount = table.columns.size
    val pkeyColumnCount = table.primaryKeyColumns.size
    val valueColumnCount = table.valueColumns.size
    val pkeyColumnCountConstId = "${id}_pkey_column_count".asId
    val pkeyColumnCountAsRust = pkeyColumnCountConstId.shout
    val valueColumnCountConstId = "${id}_value_column_count".asId
    val valueColumnCountAsRust = valueColumnCountConstId.shout
    val columnCountConstId = "${id}_column_count".asId
    val columnCountAsRust = columnCountConstId.shout
    val columnSetLiteralId = "${id}_column_set".asId
    val columnSetLiteralAsRust = columnSetLiteralId.shout

    val columnVectorDecls
        get() = table.columns.joinToString("\n") {
            "let mut ${it.nameId} = Vec::with_capacity(chunk_size);"
        }

    val columnVectorAssignments = listOf(
        table.primaryKeyColumns.joinToString("\n") { it.pushValue("key") },
        table.valueColumns.joinToString("\n") { it.pushValue("value") },
    ).joinToString("\n")

    val columnVectorClears = listOf(
        table.columns.joinToString("\n") { "${it.nameId}.clear();" },
    ).joinToString("\n")

    val formattedColumnNames = table.columns.chunked(6)
        .map { chunk -> chunk.map { it.nameId }.joinToString(", ") }
        .joinToString(",\n\t")

    val unnestedColumnExpressions = table.columns
        .withIndex()
        .chunked(6)
        .map { chunks ->
            chunks.joinToString(", ") { indexedValue ->
                "${'$'}${indexedValue.index + 1}${indexedValue.value.unnestCast}[]"
            }
        }
        .joinToString(",\n\t")

    val columnSetLiteralValue = "($formattedColumnNames)"
    val unnestedColumnExpressionId = "unnested_column_expression".asId
    val unnestedColumnExpressionValue = "($unnestedColumnExpressions)"

    val insertStatement = rustQuote(
        """insert into ${table.nameId}
{$columnSetLiteralAsRust}
values 
{values_placeholder}
returning id
"""
    )

    val bulkInsertStatement = rustQuote(
        """insert into ${table.nameId}
$columnSetLiteralValue
SELECT * FROM UNNEST
${unnestedColumnExpressionValue}
"""
    )

    val pkey = table.primaryKeyColumnNameIds
    val pkeyStructId = "${id.snake}_pkey".asId

    val valuesStructId = "${id.snake}_values".asId
    val rowTypeId = "${id.snake}_row".asId

    val clientFnParam = FnParam("client", "&tokio_postgres::Client".asType, "The tokio postgresql client")
    val rowsParam = FnParam("rows", "&[${rowTypeId.capCamel}]".asType, "Rows to insert")
    val chunkSizeFnParam = FnParam("chunk_size", USize, "How to chunk the inserts")

    val keyStruct = if (pkey.isNotEmpty()) {
        Struct(
            pkeyStructId.snake,
            "Primary key fields for `${id.capCamel}`",
            fields = table
                .primaryKeyColumns
                .map { dbColumn -> dbColumn.asRustField },
            attrs = commonDerives + derive("Default")
        )
    } else {
        null
    }

    val valuesStruct = Struct(
        valuesStructId.snake,
        "Value fields for `${id.capCamel}`",
        fields = table
            .valueColumns
            .map { dbColumn -> dbColumn.asRustField },
        attrs = commonDerives + derive("Default")
    )

    val insertBody = listOf(
        """
        use itertools::Itertools;
        let values_placeholder = rows.into_iter().enumerate().map(|(i, row)| {
            let start = 1 + i * $columnCountAsRust;
            format!("({})",
                (start..start+$columnCountAsRust)
                   .map(|param_index| format!("${'$'}{param_index}")).join(", "))
        }).join(",\n\t");
        
        let statement = format!($insertStatement);
        tracing::info!("SQL ->```\n{statement}\n```");

        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(rows.len() * $columnCountAsRust);
        for row in rows {""",
        indent(
            listOf(
                table.primaryKeyColumns.map {
                    "params.push(&row.0.${it.nameId});"
                },
                table.valueColumns.map {
                    "params.push(&row.1.${it.nameId});"
                },
            ).flatten().joinToString("\n")
        ),
        """}
            
        let results = match client.query(&statement, &params[..]).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };
        
        results.iter().for_each(|row| tracing::info!("Row id -> {:?}", row.get::<usize, i32>(0)));""",
    ).joinToString("\n")

    val selectBody = listOf(
        """
        //HERE
        """.trimIndent()
    ).joinToString("\n")

    val updateBody = listOf(
        """
            let mut statement = "update sample SET ".to_string();

        if s_clause != "" {
            statement = statement + &s_clause + " WHERE ";
        } else {
            ;
        }

        if w_clause != "" {
            statement = statement + &w_clause + " RETURNING *";
        } else {
            ;
        }

        println!("{}", statement);

        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(0);

        let results = match client.query(&statement, &params[..]).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        results
            .iter()
            .for_each(|row| {
                tracing::info!("updated row id -> {:?}", row.get::<usize, i32>(0))
            } );
        """.trimIndent()
    ).joinToString("\n")

    val deleteBody = listOf(
        """
        let col_num = ${table.columns.size};
        assert!(cols.len() == ops.len() && ops.len() == conds.len());
        assert!(cols.len() <= col_num);
        

        let mut statement = "delete from ${table.nameId} where (".to_string();

        if clause != "" {
            statement = statement + &clause + ") RETURNING *";
        } else {
            for i in 0..cols.len() - 1 {
                statement = statement + cols[i] + " " + ops[i] + " " + conds[i] + " OR ";
            }
            statement = statement
                + cols[cols.len() - 1]
                + " "
                + ops[cols.len() - 1]
                + " "
                + conds[cols.len() - 1]
                + ") RETURNING *";
        }

        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(0);

        let results = match client.query(&statement, &params[..]).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        results
            .iter()
            .for_each(|row| tracing::info!("deleted row id -> {:?}", row.get::<usize, i32>(0)));
        """.trimIndent()
    ).joinToString("\n")

    val bulkInsertFn = Fn(
        "bulk_insert",
        "Insert large batch of [${id.capCamel}] rows.",
        clientFnParam,
        rowsParam,
        chunkSizeFnParam,
        returnType = "Result<(), tokio_postgres::Error>".asType,
        body = FnBody("""
let insert_statement = $bulkInsertStatement;           
let mut chunk = 0;
$columnVectorDecls
for chunk_rows in rows.chunks(chunk_size) {
    for (key, value) in chunk_rows.into_iter() {
$columnVectorAssignments    
    }
    let chunk_result = client.execute(
        insert_statement,
        &[${table.columns.joinToString(", ") { "&${it.nameId}" }}]
    ).await;
    
    match &chunk_result {
        Err(err) => {
            tracing::error!("Failed bulk_insert `${table.nameId}` chunk({chunk}) -> {err}");
            chunk_result?;
        }
        _ => tracing::debug!("Finished inserting chunk({chunk}), size({}) in `${table.nameId}`", chunk_rows.len())
    }
    chunk += 1;
    $columnVectorClears        
}
Ok(())
        """.trimIndent()
        ),
        isAsync = true,
        hasTokioTest = true,
        testFnAttrs = attrSerializeTest.asAttrList
    )

    val bulkUpsertFn = Fn(
        "bulk_upsert",
        "Upsert large batch of [${id.capCamel}] rows.",
        clientFnParam,
        rowsParam,
        chunkSizeFnParam,
        testFnAttrs = attrSerializeTest.asAttrList
    )

    val tableStruct = Struct(
        "table_${id.snake}",
        """Table Gateway Support for table `${id.snake}`.
            |Rows
        """.trimMargin(),
        typeImpl = TypeImpl(
            "Table${id.capCamel}".asType,
            Fn(
                "insert",
                "Insert rows of `${id.snake}`",
                clientFnParam,
                rowsParam,
                isAsync = true,
                hasTokioTest = true,
                body = FnBody(insertBody),
                testFnAttrs = attrSerializeTest.asAttrList
            ),
            bulkInsertFn,
            bulkUpsertFn,
            Fn(
                "select",
                "Select rows of `${id.snake}`",
                clientFnParam,
                isAsync = true,
                hasTokioTest = true,
                body = FnBody(selectBody),
                testFnAttrs = attrSerializeTest.asAttrList
            ),
            Fn(
                "update",
                "Update rows of `${id.snake}`",
                clientFnParam,
                FnParam("s_clause", "String".asType, "clause for SET statement"),
                FnParam("w_clause", "String".asType, "clause for WHERE statement"),
                isAsync = true,
                hasTokioTest = true,
                body = FnBody(updateBody),
                testFnAttrs = attrSerializeTest.asAttrList
            ),
            Fn(
                "delete",
                "Delete rows of `${id.snake}`",
                clientFnParam,
                FnParam("clause", "String".asType, "full clause, skips input vectors"),
                FnParam("cols", "Vec::<&str>".asType, "columns list for clause"),
                FnParam("ops", "Vec::<&str>".asType, "operator list for clause"),
                FnParam("conds", "Vec::<&str>".asType, "conditions list for clause"),
                isAsync = true,
                hasTokioTest = true,
                body = FnBody(deleteBody),
                testFnAttrs = attrSerializeTest.asAttrList
            ),
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
        structs = listOfNotNull(keyStruct, valuesStruct, tableStruct),
        consts = listOf(
            Const(
                columnSetLiteralId.snake,
                "Column names",
                "&'static str".asType,
                columnSetLiteralValue
            ),
            Const(
                unnestedColumnExpressionId.snake,
                "Unnest column expressions",
                "&'static str".asType,
                unnestedColumnExpressionValue
            ),
            Const(
                pkeyColumnCountConstId.snake,
                "Total number of columns, primary key columns and non-key columns",
                USize,
                pkeyColumnCount
            ),
            Const(
                valueColumnCountConstId.snake,
                "Total number of columns in the primary key",
                USize,
                valueColumnCount
            ),
            Const(
                columnCountConstId.snake,
                "Total number of columns",
                USize,
                columnCount
            )
        ),
        typeAliases = listOf(
            TypeAlias(
                rowTypeId.snake,
                "(${pkeyStructId.asStructName}, ${valuesStructId.asStructName})".asType,
                doc = "Rows are composed of the primary key and the value fields",
                visibility = Visibility.PubExport
            )
        )
    )

}

fun main() {

    println("TableGatewayGenerator")
}
