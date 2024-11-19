package kgen.rust.db

import kgen.asId
import kgen.db.DbTable

val DbTable.groupedColumnNames
    get() = this.columns.chunked(6)
        .map { chunk -> chunk.map { it.nameId }.joinToString(", ") }
        .joinToString(",\n\t")

val DbTable.nonAutoIncGroupedColumnNames
    get() = this.nonAutoIncColumns.chunked(6)
        .map { chunk -> chunk.map { it.nameId }.joinToString(", ") }
        .joinToString(",\n\t")

val DbTable.unnestedColumnExpressions get() = this.nonAutoIncColumns
    .withIndex()
    .chunked(6)
    .map { chunks ->
        chunks.joinToString(", ") { indexedValue ->
            "${'$'}${indexedValue.index + 1}${indexedValue.value.unnestCast}[]"
        }
    }
    .joinToString(",\n\t")

val DbTable.pkeyAsSql get() = this.primaryKeyColumns.chunked(6)
        .joinToString(", ") { chunk ->
            chunk.joinToString(", ") { it.nameId.asId.snake }
        }

val DbTable.unnestColumnVectorDecls
    get() = this.nonAutoIncColumns.joinToString("\n") {
        "let mut ${it.nameId} = Vec::with_capacity(chunk_size);"
    }

val DbTable.bulkUpdateUnnestAssignments get() = listOf(
    this.nonAutoIncColumns.joinToString("\n") { it.pushValue("row")  },
).joinToString("\n")

val DbTable.bulkUnnestClearStatements get() = listOf(
    this.nonAutoIncColumns.joinToString("\n") { "${it.nameId}.clear();" },
).joinToString("\n")
