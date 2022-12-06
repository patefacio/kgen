package kgen.rust

import kgen.*

data class Enum(
    val nameId: String,
    override val doc: String = missingDoc(nameId, "Enum"),
    val values: List<EnumValue>,
    val visibility: Visibility = Visibility.Pub,
    val attrs: AttrList = AttrList(),
    val uses: Set<Use> = emptySet()
) : Identifier(nameId), Type, AsRust {

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

    val enumName = id.capCamel

    override val asRustName: String
        get() = enumName

    override val type: String
        get() = enumName

    override val asRust: String
        get() = listOfNotNull(
            attrs.asOuterAttr,
            "${commentTriple(doc)}\n${trailingText(visibility.asRust)}enum $asRustName {\n${
                indent(values.joinToString(",\n") { it.asRust })
            }\n}"
        ).joinNonEmpty()
}

