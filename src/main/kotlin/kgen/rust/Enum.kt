package kgen.rust

import kgen.Identifier
import kgen.commentTriple
import kgen.indent
import kgen.missingDoc

data class Enum(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Enum"),
    val values: List<EnumValue>,
) : Identifier(nameId), AsRust {

    constructor(nameId: String, doc: String = missingDoc(nameId, "Enum"), vararg values: EnumValue) : this(
        nameId,
        doc,
        values.toList()
    )

    override val asRust: String
        get() = "${commentTriple(doc)}\nenum ${id.capCamel} {\n${
            indent(values.joinToString(",\n") { it.asRust })
        }\n}"
}

