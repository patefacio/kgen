package kgen.rust

import kgen.*

data class Enum(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Enum"),
    val values: List<EnumValue>,
    val visibility: Visibility = Visibility.None,
) : Identifier(nameId), AsRust {

    constructor(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum"),
        vararg values: EnumValue,
        visibility: Visibility
    ) : this(
        nameId,
        doc,
        values.toList(),
        visibility
    )

    override val asRust: String
        get() = "${commentTriple(doc)}\n${trailingText(visibility.asRust)}enum ${id.capCamel} {\n${
            indent(values.joinToString(",\n") { it.asRust })
        }\n}"
}

