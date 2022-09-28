package kgen.rust

import kgen.commentTriple
import kgen.indent

data class Enum(
    val nameId: String,
    val doc: String = defaultDoc(nameId),
    val values: List<EnumValue>,
) : Identifiable(nameId), AsRust {

    companion object {
        fun defaultDoc(nameId: String) = "TODO: Document Enum($nameId)"
    }

    constructor(nameId: String, doc: String = defaultDoc(nameId), vararg values: EnumValue) : this(
        nameId,
        doc,
        values.toList()
    )

    override val asRust: String
        get() = "${commentTriple(doc)}\nenum ${id.capCamel} {\n${
            indent(values.joinToString(",\n") { it.asRust })
        }\n}"
}

