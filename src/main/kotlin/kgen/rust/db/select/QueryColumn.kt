package kgen.rust.db.select

import kgen.Id
import kgen.db.DbColumn
import kgen.db.DbType

sealed class QueryColumn {

    abstract val id: Id
    abstract val expression: String
    abstract val doc: String?
    abstract val type: DbType
    abstract val isAutoInc: Boolean
    abstract val dbColumn: DbColumn?

    data class Column(override val dbColumn: DbColumn) : QueryColumn() {
        override val id get() = dbColumn.id
        override val expression: String
            get() = dbColumn.id.snake
        override val doc: String?
            get() = dbColumn.doc ?: "Corresonds to column `${dbColumn.id}` of type ${dbColumn.type}"
        override val type: DbType get() = dbColumn.type
        override val isAutoInc get() = dbColumn.isAutoIncrement
    }

    data class Expression(
        override val dbColumn: DbColumn? = null,
        override val expression: String,
        override val type: DbType,
        override val id: Id,
        override val doc: String? = null,
    ) : QueryColumn() {
        override val isAutoInc get() = false
    }

    companion object {
        fun fromDbColumn(column: DbColumn) = Column(column)
    }

    val rustType get() = type.asRustType
    val rustTypeName get() = rustType.asRustName
    fun fieldAccess(fromVar: String) = "${fromVar}.${id.snake}"
    fun columnReadAccess(fromVar: String, columnIndex: Int) = "${fromVar}.get($columnIndex)"
    fun readAccess(fromVar: String) = "${fromVar}.${id.snake}"
}

val DbColumn.asQueryColumn: QueryColumn get() = QueryColumn.fromDbColumn(this)
