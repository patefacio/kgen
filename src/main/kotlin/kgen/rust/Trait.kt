package kgen.rust

import kgen.*

/**
 * Represents a trait in Rust, including its name, functions, associated types,
 * supertraits, and related attributes.
 */
interface AsSuperTrait {
    /**
     * Returns the Rust representation of this supertrait.
     */
    val asSuperTrait: String
}

/**
 * Represents a Rust trait with its functions, generic parameters, visibility,
 * associated types, supertraits, and attributes.
 *
 * This class provides utilities to generate the Rust representation of the trait.
 *
 * @property nameId The snake case name for the trait.
 * @property doc The documentation string associated with the trait. Defaults to a placeholder if not provided.
 * @property functions A list of functions declared in the trait. Defaults to an empty list.
 * @property genericParamSet The set of generic parameters associated with the trait. Defaults to an empty set.
 * @property visibility The visibility level of the trait (e.g., public or private). Defaults to `Pub`.
 * @property associatedTypes A list of associated types declared in the trait. Defaults to an empty list.
 * @property superTraits A list of supertraits that this trait extends. Defaults to an empty list.
 * @property uses A set of `use` statements required by the trait. Defaults to an empty set.
 * @property attrs A list of attributes applied to the trait. Defaults to an empty `AttrList`.
 */
data class Trait(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Trait"),
    val functions: List<Fn> = emptyList(),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val visibility: Visibility = Visibility.Pub,
    val associatedTypes: List<AssociatedType> = emptyList(),
    val superTraits: List<AsSuperTrait> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val attrs: AttrList = AttrList()
) : Identifier(nameId), AsRust, AsSuperTrait {

    /**
     * Secondary constructor that accepts functions as a vararg parameter.
     *
     * @param nameId The unique identifier (name) for the trait.
     * @param doc The documentation string associated with the trait.
     * @param functions The functions declared in the trait as a vararg parameter.
     * @param genericParamSet The set of generic parameters associated with the trait.
     * @param visibility The visibility level of the trait.
     * @param associatedTypes A list of associated types declared in the trait.
     * @param superTraits A list of supertraits that this trait extends.
     * @param uses A set of `use` statements required by the trait.
     * @param attrs A list of attributes applied to the trait.
     */
    constructor(
        nameId: String,
        doc: String = missingDoc(nameId, "Trait"),
        vararg functions: Fn,
        genericParamSet: GenericParamSet = GenericParamSet(),
        visibility: Visibility = Visibility.Pub,
        associatedTypes: List<AssociatedType> = emptyList(),
        superTraits: List<AsSuperTrait> = emptyList(),
        uses: Set<Use> = emptySet(),
        attrs: AttrList = AttrList()
    ) : this(
        nameId,
        doc,
        functions.toList(),
        genericParamSet = genericParamSet,
        visibility = visibility,
        associatedTypes = associatedTypes,
        superTraits = superTraits,
        uses = uses,
        attrs = attrs
    )

    /**
     * Aggregates all `use` statements required by the trait, including those from its functions.
     */
    val allUses get() = uses + functions.map { it.allUses }.flatten()

    /**
     * Returns the Rust-style name of the trait.
     */
    val asRustName get() = id.capCamel

    /**
     * Returns the Rust representation of this trait as a supertrait.
     */
    override val asSuperTrait: String
        get() = asRustName

    /**
     * Generates a list of functions scoped to the trait, marking them with trait-specific visibility.
     */
    private val traitScopedFunctions get() = functions.map {
        it.copy(blockName = "trait fn ${id.capCamel}::${it.blockName}").copy(visibility = Visibility.None)
    }

    /**
     * Generates the content of the Rust trait, including associated types and functions.
     */
    private val traitContent get() = indent(
        (associatedTypes.map { it.asRust } +
                traitScopedFunctions.map { it.asTraitFn }).joinNonEmpty("\n\n")
    )!!

    /**
     * Generates the Rust declaration for the trait's supertraits, if any.
     */
    private val superTraitDecl
        get() = if (superTraits.isNotEmpty()) {
            ": ${superTraits.joinToString(" + ") { it.asSuperTrait }}"
        } else {
            ""
        }

    /**
     * Generates the Rust representation of this trait.
     */
    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            withWhereClause(
                "${trailingText(visibility.asRust)}trait ${id.capCamel}${genericParamSet.asRust}$superTraitDecl",
                genericParamSet
            ) + " {",
            emptyIfOnlyWhitespace(traitContent),
            "}"
        ).joinNonEmpty()
}

/**
 * Represents a Rust supertrait that has not been explicitly modeled.
 *
 * @property asSuperTrait The Rust representation of the supertrait.
 */
data class UnmodeledTrait(override val asSuperTrait: String) : AsSuperTrait

/**
 * Utility function to create a list of unmodeled supertraits from their names.
 *
 * @param superTraits The names of the supertraits.
 * @return A list of [UnmodeledTrait] instances representing the supertraits.
 */
fun unmodeledSuperTraits(vararg superTraits: String) = superTraits.map { UnmodeledTrait(it) }

/**
 * Extension property to convert a string into an [UnmodeledTrait].
 */
val String.asSuperTrait get() = UnmodeledTrait(this)

/**
 * Extension property to convert a list of strings into a list of [UnmodeledTrait].
 */
val List<String>.asSuperTraits get() = this.map { UnmodeledTrait(it) }
