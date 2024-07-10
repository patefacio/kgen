package kgen.rust.db

import kgen.asId
import kgen.db.*
import kgen.rust.*

val DbColumn.asRustType get() = when(this.type) {

    is DbType.Byte -> U8
    is DbType.Double -> F64
    is DbType.Integer -> I64
    is DbType.SmallInteger -> I16
    is DbType.BigInteger -> I64
    is DbType.Text -> RustString
    is DbType.Date -> "chrono::NaiveDate".asType
    is DbType.DateTime, is DbType.Timestamp -> "chrono::NaiveDateTime".asType
    is DbType.IntegerAutoInc -> I64
    is DbType.LongAutoInc -> I64
    is DbType.UlongAutoInc -> U64
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
        "Primary key fields for `${id.capCamel}`",
        fields = table
            .columns
            .filter { !table.primaryKeyColumnNames.contains(it.nameId) }
            .map { dbColumn -> dbColumn.asRustField }
    )
    /*
    val insertBody = listOf(
        "//i",
        table.columns.map { column ->
        "// ${column.nameId}"
        }.joinToString(",\n")
    ).joinToString("\n")
     */
    //val insertBody = "let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(34);"
    val insertBody = "let col_num = rows.len();\n" +
            "        let mut values_str = \"\";\n" +
            "        for row in rows {\n" +
            "            values_str += row[1];\n" +
            "        }"

    //Rust for sample.rs
    /*
    let col_num: usize = 8;
        let row_num: usize = rows.len();

        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(col_num*row_num);

        let mut value_str:String = "(".to_string();
        for j in 1..row_num {
            for i in 1..col_num + 1 {
                value_str.push_str(&format!("${}, ", i * j));
            }
            value_str.push_str("),");
        }
        //println!("{}", value_str);

        let mut params_str: String = "".to_string();
        for row in rows {
            let (sp, sv) = row;
            //for loop??
            let temp_str = format!("({}, {}, {}, {}, {}, {}, {}, {}),", &sv.the_name, &sv.the_small_int, &sv.the_large_int, &sv.general_int, &sv.the_date, &sv.the_date_time, &sv.the_uuid, &sv.the_ulong);
            params_str.push_str(&temp_str);


            params.push(&sv.the_name);
            params.push(&sv.the_small_int);
            params.push(&sv.the_large_int);
            params.push(&sv.general_int);
            params.push(&sv.the_date);
            params.push(&sv.the_date_time);
            params.push(&sv.the_uuid);
            params.push(&sv.the_ulong);
        }



        let statement: String = format!(r#"insert into sample
            (the_name, the_small_int, the_large_int, general_int, the_date, the_date_time, the_uuid, the_ulong)
            values
            {value_str}
            returning id
            "#);

        let results = match client.query(&statement, &params[..]).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        results.iter().for_each(|row| tracing::info!("Row id -> {:?}", row.get::<usize, i32>(0)));
     */

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
