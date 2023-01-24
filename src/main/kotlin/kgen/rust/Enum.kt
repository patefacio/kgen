package kgen.rust

import kgen.*

data class Enum(
    val nameId: String,
    override val doc: String = missingDoc(nameId, "Enum"),
    val values: List<EnumValue>,
    val visibility: Visibility = Visibility.Pub,
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val attrs: AttrList = AttrList(),
    val uses: Set<Use> = emptySet(),
    val typeImpl: TypeImpl? = null,
    val implementedTraits: List<Trait> = emptyList()
    ) : Identifier(nameId), Type, AsRust {

    constructor(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum"),
        vararg values: EnumValue,
        visibility: Visibility = Visibility.Pub,
        genericParamSet: GenericParamSet = GenericParamSet(),
        attrs: AttrList = AttrList(),
        uses: Set<Use> = emptySet(),
        typeImpl: TypeImpl? = null,
        implementedTraits: List<Trait> = emptyList()
    ) : this(
        nameId,
        doc,
        values.toList(),
        visibility,
        genericParamSet,
        attrs,
        uses,
        typeImpl,
        implementedTraits
    )

    val enumName = id.capCamel

    val traitImpls
        get() = implementedTraits.map { trait ->
            TraitImpl(enumName.asType, trait, genericParamSet = genericParamSet)
        }

    override val asRustName: String
        get() = enumName

    override val type: String
        get() = enumName

    override val asRust: String
        get() = listOfNotNull(
            commentTriple(doc),
            attrs.asOuterAttr,
            withWhereClause(
                "${trailingText(visibility.asRust)}enum $asRustName${genericParamSet.asRust}",
                genericParamSet
            ) + " {",
            indent(values.joinToString(",\n") { it.asRust }),
            "}"
        ).joinNonEmpty()
}

