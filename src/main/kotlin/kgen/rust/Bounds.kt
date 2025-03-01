package kgen.rust

/**
 * Represents Rust generic bounds, including lifetimes and trait bounds, for use in type definitions and constraints.
 *
 * This class allows the construction and manipulation of Rust-style bounds, including lifetimes (`'a`) and
 * trait bounds (e.g., `T: Trait`). It provides utilities to combine bounds, check for emptiness, and
 * generate the Rust syntax representation.
 *
 * @property lifetimes A list of lifetimes associated with the bounds (e.g., `'a, 'b`). Defaults to an empty list.
 * @property traitBounds A list of trait bounds associated with the bounds (e.g., `T: Trait`). Defaults to an empty list.
 */
data class Bounds(
    val lifetimes: List<Lifetime> = emptyList(),
    val traitBounds: List<TraitBound> = emptyList()
) : AsRust {

    /**
     * Secondary constructor for creating bounds from a vararg of [Lifetime]s.
     *
     * @param lifetimes A variable number of lifetimes to be included in the bounds.
     */
    constructor(vararg lifetimes: Lifetime) : this(lifetimes = lifetimes.toList())

    /**
     * Secondary constructor for creating bounds from a vararg of [TraitBound]s, with optional lifetimes.
     *
     * @param traitBounds A variable number of trait bounds to be included.
     * @param lifetimes A list of lifetimes to be included. Defaults to an empty list.
     */
    constructor(
        vararg traitBounds: TraitBound,
        lifetimes: List<Lifetime> = emptyList()
    ) : this(traitBounds = traitBounds.toList(), lifetimes = lifetimes)

    /**
     * Secondary constructor for creating bounds from a vararg of trait names as strings, with optional lifetimes.
     *
     * The strings are converted to [TraitBound.Unmodeled] instances.
     *
     * @param traitBounds A variable number of trait names as strings to be included as bounds.
     * @param lifetimes A list of lifetimes to be included. Defaults to an empty list.
     */
    constructor(
        vararg traitBounds: String,
        lifetimes: List<Lifetime> = emptyList()
    ) : this(traitBounds = traitBounds.toList().map { TraitBound.Unmodeled(it) }, lifetimes = lifetimes)

    /**
     * Generates the Rust string representation of the bounds.
     *
     * The representation combines lifetimes and trait bounds, separated by `+`.
     *
     * Example:
     * ```
     * "'a + 'b + Trait1 + Trait2"
     * ```
     */
    override val asRust: String
        get() = (lifetimes.map { it.asRust } + traitBounds.map { it.asRust })
            .joinToString(" + ")

    /**
     * Combines two bounds into a new `Bounds` object by appending their lifetimes and trait bounds.
     *
     * @param bounds The bounds to be added to this instance.
     * @return A new `Bounds` instance containing the combined lifetimes and trait bounds.
     */
    operator fun plus(bounds: Bounds) =
        Bounds(
            lifetimes = this.lifetimes + bounds.lifetimes,
            traitBounds = this.traitBounds + bounds.traitBounds
        )

    /**
     * Checks whether the bounds are empty (i.e., contain no lifetimes or trait bounds).
     *
     * @return `true` if the bounds are empty, otherwise `false`.
     */
    fun isEmpty() = lifetimes.isEmpty() && traitBounds.isEmpty()
}

/**
 * Extension property to convert a list of strings into a list of [TraitBound.Unmodeled].
 *
 * Example:
 * ```
 * val traitBounds = listOf("Debug", "Clone").asTraitBounds
 * ```
 */
val List<String>.asTraitBounds get() = this.map { TraitBound.Unmodeled(it) }

/**
 * Extension property to convert a list of strings into `Bounds` containing lifetimes.
 *
 * Example:
 * ```
 * val lifetimes = listOf("'a", "'b").asLifetimeBounds
 * ```
 */
val List<String>.asLifetimeBounds get() = Bounds(lifetimes = this.map { it.asLifetime })

/**
 * Extension property to convert a list of strings into `Bounds` containing trait bounds.
 *
 * Example:
 * ```
 * val bounds = listOf("Debug", "Clone").asBounds
 * ```
 */
val List<String>.asBounds get() = Bounds(traitBounds = this.asTraitBounds)
