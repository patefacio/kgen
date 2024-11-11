package kgen.rust.db

import kgen.Id
import kgen.db.DbColumn
import kgen.db.DbType

sealed class QueryColumn {

    abstract val id: Id
    abstract val expression: String
    abstract val doc: String?
    abstract val type: DbType

    data class Column(val column: DbColumn) : QueryColumn() {
        override val id get() = column.id
        override val expression: String
            get() = column.id.snake
        override val doc: String? get() = column.doc
        override val type: DbType get() = column.type
    }

    data class Expression(
        override val expression: String,
        override val type: DbType,
        override val id: Id,
        override val doc: String? = null,
    ) : QueryColumn() {
    }

    companion object {
        fun fromDbColumn(column: DbColumn) = Column(column)
    }
}

val DbColumn.asQueryColumn: QueryColumn get() = QueryColumn.fromDbColumn(this)
