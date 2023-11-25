package kgen.rust

data class Bounds(
    val lifetimes: List<Lifetime> = emptyList(),
    val traitBounds: List<TraitBound> = emptyList()
) : AsRust {

    constructor(vararg lifetimes: Lifetime) : this(lifetimes = lifetimes.toList())

    constructor(
        vararg traitBounds: TraitBound,
        lifetimes: List<Lifetime> = emptyList()
    ) : this(traitBounds = traitBounds.toList(), lifetimes = lifetimes)

    constructor(
        vararg traitBounds: String,
        lifetimes: List<Lifetime> = emptyList()
    ) : this(traitBounds = traitBounds.toList().map { TraitBound.Unmodeled(it) }, lifetimes = lifetimes)

    override val asRust: String
        get() = (lifetimes.map { it.asRust } + traitBounds.map { it.asRust })
            .joinToString(" + ")

    operator fun plus(bounds: Bounds) =
        Bounds(
            lifetimes = this.lifetimes + bounds.lifetimes,
            traitBounds = this.traitBounds + bounds.traitBounds
        )

    fun isEmpty() = lifetimes.isEmpty() && traitBounds.isEmpty()
}

val List<String>.asTraitBounds get() = this.map { TraitBound.Unmodeled(it) }

val List<String>.asLifetimeBounds get() = Bounds(lifetimes = this.map { it.asLifetime })
val List<String>.asBounds get() = Bounds(traitBounds = this.asTraitBounds)