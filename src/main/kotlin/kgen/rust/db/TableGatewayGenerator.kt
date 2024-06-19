package kgen.rust.db

import kgen.asId
import kgen.db.*
import kgen.rust.*

val DbColumn.asRustField
    get() = Field(
        this.nameId.asId.snake,
        doc = this.doc ?: "Field for column `$nameId`",
        "TODO".asType
    )

data class TableGatewayGenerator(
    val table: DbTable
) {

    val id = table.nameId.asId
    val pkey = table.primaryKeyColumnNames

    val pkeyStructId = "${id.snake}_pkey".asId
    val valuesStructId = "${id.snake}_values".asId


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

    val tableStruct = Struct(
        "table_${id.snake}",
        "Table Gateway Support for table `${id.snake}`.",
        typeImpl = TypeImpl(
            "Table${id.capCamel}".asType,
            Fn(
                "insert",
                "Insert rows of `${id.snake}`"
            ),

            Fn(
                "select",
                "Select rows of `${id.snake}`"
            ),


            Fn(
                "update",
                "Update rows of `${id.snake}`"
            ),

            Fn(
                "delete",
                "Delete rows of `${id.snake}`"
            ),
        )
    )

    val asModule = Module(
        table.nameId,
        """Table gateway pattern implemented for ${id.capCamel}""",
        structs = listOfNotNull(keyStruct, valuesStruct, tableStruct)
    )

}

fun main() {


    val gooTable = ModeledTable(
        "goo",
        columns = listOf(
            ModeledColumn("id", type = DbType.IntegerAutoInc),
            ModeledColumn("name", type = DbType.VarChar(16)),
            ModeledColumn("blob", type = DbType.Blob),
            ModeledColumn("weight_double", type = DbType.Double),
            ModeledColumn("weight_int", type = DbType.Integer)
        ),
        primaryKeyColumnNames = setOf("id")
    )

    println(TableGatewayGenerator(gooTable).asModule.asRust)

}
