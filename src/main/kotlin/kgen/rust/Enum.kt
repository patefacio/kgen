package kgen.rust

import kgen.*

/** A [rust enumeration](https://doc.rust-lang.org/reference/items/enumerations.html)
 *
 * @property nameId Snake case name for enum
 * @property doc The comment for the enum
 * @property variants The enum variants
 * @property visibility The visibility for the enum
 * @property attrs Rust attributes associated with the enum
 * @property uses Additional import requirements by types referenced directly by the enum
 * @property typeImpl The rust impl for this enum
 * @property traitImpls This enum's implementations for various [Trait]s
 * @property implementedTraits Rust traits implemented by this enum
 *
 */
data class Enum(
    val nameId: String,
    override val doc: String = missingDoc(nameId, "Enum"),
    val variants: List<Variant>,
    val visibility: Visibility = Visibility.Pub,
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val attrs: AttrList = AttrList(),
    val uses: Set<Use> = emptySet(),
    val typeImpl: TypeImpl? = null,
    val traitImpls: List<TraitImpl> = emptyList(),
    val implementedTraits: List<Trait> = emptyList()
    ) : Identifier(nameId), Type, AsRust {

    constructor(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum"),
        vararg values: Variant,
        visibility: Visibility = Visibility.Pub,
        genericParamSet: GenericParamSet = GenericParamSet(),
        attrs: AttrList = AttrList(),
        uses: Set<Use> = emptySet(),
        typeImpl: TypeImpl? = null,
        traitImpls: List<TraitImpl> = emptyList(),
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
        traitImpls,
        implementedTraits
    )

    /** Name of enum as Rust Name (Cap Camel) */
    override val asRustName: String
        get() = id.capCamel

    /** The enum as rust type name */
    override val type: String
        get() = asRustName

    /** List of all trait implementations */
    val allTraitImpls
        get() = traitImpls + implementedTraits.map { trait ->
            TraitImpl(id.capCamel.asType, trait, genericParamSet = genericParamSet)
        }


    /** The enum as rust code */
    override val asRust: String
        get() = listOfNotNull(
            commentTriple(doc),
            attrs.asOuterAttr,
            withWhereClause(
                "${trailingText(visibility.asRust)}enum $asRustName${genericParamSet.asRust}",
                genericParamSet
            ) + " {",
            indent(variants.joinToString(",\n") { it.asRust }),
            "}"
        ).joinNonEmpty()
}

