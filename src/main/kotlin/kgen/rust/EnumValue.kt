package kgen.rust

import kgen.Identifier
import kgen.commentTriple
import kgen.indent
import kgen.trailingText

sealed class EnumValue(val nameId: String, val doc: String) : AsRust, Identifier(nameId) {

    val docComment get() = trailingText(commentTriple(doc), "\n")

    class UnitStruct(nameId: String, doc: String = "TODO Document UnitStruct($nameId)") : EnumValue(nameId, doc) {
        override val asRust: String
            get() = "${docComment}${id.capCamel}"
    }

    class TupleStruct(nameId: String, doc: String = "TODO Document TupleStruct($nameId)", val types: List<Type>) :
        EnumValue(nameId, doc) {

        constructor(nameId: String, doc: String = "TODO Document TupleStruct($nameId)", vararg types: Type) : this(
            nameId,
            doc,
            types.toList()
        )

        override val asRust: String
            get() = "$docComment${id.capCamel}(${types.joinToString { it.asRust }})"
    }

    class Struct(nameId: String, doc: String = "TODO Document Struct($nameId)", val fields: List<Field>) :
        EnumValue(nameId, doc) {

        constructor(nameId: String, doc: String = "TODO Document Struct($nameId)", vararg fields: Field) : this(
            nameId,
            doc,
            fields.toList()
        )

        override val asRust: String
            get() = "$docComment${id.capCamel} {\n${indent(fields.joinToString(",\n") { it.asRust })}\n}"
    }
}