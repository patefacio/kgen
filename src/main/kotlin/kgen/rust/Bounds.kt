package kgen.rust

data class Bounds(
    val lifetimes: List<Lifetime> = emptyList(),
    val traitBounds: List<TraitBound> = emptyList()
) : AsRust {

    constructor(vararg lifetimes: Lifetime) : this(lifetimes = lifetimes.toList())

    constructor(vararg traitBounds: TraitBound) : this(traitBounds = traitBounds.toList())

    constructor(vararg traitBounds: String) : this(traitBounds = traitBounds.toList().map { TraitBound.Unmodeled(it) })

    override val asRust: String
        get() = (lifetimes.map { asRust } + traitBounds.map { it.asRust })
            .joinToString(" + ")

    fun isEmpty() = lifetimes.isEmpty() && traitBounds.isEmpty()
}

val List<String>.asTraitBounds get() = this.map { TraitBound.Unmodeled(it)}
val List<String>.asBounds get() = Bounds(traitBounds = this.asTraitBounds)