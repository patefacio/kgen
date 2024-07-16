package kgen.rust.db

import kgen.asId
import kgen.db.*
import kgen.indent
import kgen.rust.*

val DbColumn.asRustType get() = when(this.type) {

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
    is DbType.Uuid -> "uuid::Uuid".asType
    is DbType.Json, is DbType.VarChar -> RustString

    else -> throw(Exception("Unsupported rust type for $this"))
}

val DbColumn.asRustField
    get() = Field(
        this.nameId.asId.snake,
        doc = this.doc ?: "Field for column `$nameId`",
        this.asRustType
    )

data class TableGatewayGenerator(
    val table: DbTable
) {

    val id = table.nameId.asId
    val pkey = table.primaryKeyColumnNames

    val pkeyStructId = "${id.snake}_pkey".asId
    val valuesStructId = "${id.snake}_values".asId
    val rowTypeId = "${id.snake}_row".asId


    val keyStruct = if (pkey.isNotEmpty()) {
        Struct(
            pkeyStructId.snake,
            "Primary key fields for `${id.capCamel}`",
            fields = table
                .columns
                .filter { pkey.contains(it.nameId) }
                .map { dbColumn -> dbColumn.asRustField }
        )
    } else {
        null
    }

    val valuesStruct = Struct(
        valuesStructId.snake,
        "Value fields for `${id.capCamel}`",
        fields = table
            .columns
            .filter { !table.primaryKeyColumnNames.contains(it.nameId) }
            .map { dbColumn -> dbColumn.asRustField }
    )

    val insertBody = listOf(

        """
        let col_num = ${table.columns.size};
        let row_num = rows.len();
        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(row_num * col_num);

        let mut values_placeholder = "".to_string();
        for i in 0..row_num {
            values_placeholder += "(";
            for j in 1..col_num {
                values_placeholder += ${"&format!(\"\${}, \", j+col_num*i);"}
            }
            values_placeholder += ${"&format!(\"\${}),\\n\", col_num*(i+1));"}
        }
        values_placeholder = values_placeholder[0..values_placeholder.len()-2].to_string();

        //println!("{values_placeholder}");
        
        let statement = format!(r#" """,
            indent(listOf(
                "insert into ${table.nameId} (",
                indent(table.columns.chunked(6)
                    .map { chunk -> chunk.map { it.nameId }.joinToString(", ") }
                    .joinToString(",\n")),
                ")"
            ).joinToString("\n"), indent = "        " ),
        """        values
        {values_placeholder}
        returning id
        "#
        ${")."}to_string();

        for row in rows {""",
        indent(

            table.columns.map { column ->
                if (column.nameId in table.primaryKeyColumnNames) {
                    "params.push(&row.0.${column.nameId})"
                } else {
                    "params.push(&row.1.${column.nameId})"
                }
            }.joinToString(";\n")
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
                FnParam("client", "tokio_postgres::Client".asType, "The tokio postgresl client"),
                FnParam("rows", "&[${rowTypeId.capCamel}]".asType, "Rows to insert"),
                isAsync = true,
                hasTokioTest = true,
                body = FnBody(insertBody)

            ),
            Fn(
                "select",
                "Select rows of `${id.snake}`",
                isAsync = true,
                hasTokioTest = true
            ),


            Fn(
                "update",
                "Update rows of `${id.snake}`",
                isAsync = true,
                hasTokioTest = true
            ),

            Fn(
                "delete",
                "Delete rows of `${id.snake}`",
                isAsync = true,
                hasTokioTest = true
            ),
        )
    )

    val asModule = Module(
        table.nameId,
        """Table gateway pattern implemented for ${id.capCamel}""",
        uses = listOf("tokio_postgres::types::{Date, FromSql, ToSql}", "chrono::{NaiveDate, NaiveDateTime}").asUses,
        structs = listOfNotNull(keyStruct, valuesStruct, tableStruct),
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

}
