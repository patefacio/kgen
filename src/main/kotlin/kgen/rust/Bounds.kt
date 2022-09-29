package kgen.rust

data class Bounds(
    val lifetimes: List<Lifetime> = emptyList(),
    val traitBounds: List<TraitBound> = emptyList()
) : AsRust {

    constructor(vararg lifetimes: Lifetime) : this(lifetimes = lifetimes.toList())

    constructor(vararg traitBounds: TraitBound) : this(traitBounds = traitBounds.toList())

    override val asRust: String
        get() = (lifetimes.map { asRust } + traitBounds.map { it.asRust })
            .joinToString(", ")

    fun isEmpty() = lifetimes.isEmpty() && traitBounds.isEmpty()
}