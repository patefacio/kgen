package kgen.rust

import kgen.*

data class Struct(
    val nameId: String,
    override val doc: String = missingDoc(nameId, "Struct"),
    val fields: List<Field> = emptyList(),
    val visibility: Visibility = Visibility.None,
    val uses: Set<Use> = emptySet(),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val attrs: AttrList = AttrList()
) : Identifier(nameId), Type, AsRust {

    val structName = id.capCamel

    override val asRustName: String
        get() = structName

    constructor(
        nameId: String,
        doc: String,
        vararg fields: Field,
        visibility: Visibility = Visibility.None,
        uses: Set<Use> = emptySet(),
        genericParamSet: GenericParamSet = GenericParamSet(),
        attrs: AttrList = AttrList()
    ) : this(nameId, doc, fields.toList(), visibility, uses, genericParamSet, attrs)

    private val header
        get() =
            withWhereClause(
                "${trailingText(visibility.asRust)}struct ${structName}${genericParamSet.asRust}",
                genericParamSet
            ) + " {"

    override val type: String
        get() = structName

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            attrs.asRust,
            header,
            if (fields.isEmpty()) {
                ""
            } else {
                indent(
                    fields.joinToString(",\n") { it.asRust },
                ) ?: ""
            },
            "}"
        ).joinNonEmpty()

}