package kgen.rust

import kgen.*

data class Enum(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Enum"),
    val values: List<EnumValue>,
    val visibility: Visibility = Visibility.Pub,
    val attrs: AttrList = AttrList()
) : Identifier(nameId), AsRust {

    constructor(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum"),
        vararg values: EnumValue,
        visibility: Visibility = Visibility.Pub,
        attrs: AttrList = AttrList()
    ) : this(
        nameId,
        doc,
        values.toList(),
        visibility,
        attrs
    )

    override val asRust: String
        get() = listOfNotNull(
            attrs.asOuterAttr,
            "${commentTriple(doc)}\n${trailingText(visibility.asRust)}enum ${id.capCamel} {\n${
                indent(values.joinToString(",\n") { it.asRust })
            }\n}"
        ).joinNonEmpty()
}

