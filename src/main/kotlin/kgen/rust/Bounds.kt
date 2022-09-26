package kgen.rust

import kgen.joinNonEmpty

data class Bounds(
    val lifetimes: List<Lifetime> = emptyList(),
    val traits: List<TraitBound> = emptyList()
) : AsRust {

    override val asRust: String
        get() = (lifetimes.map { asRust } + traits.map { it.asRust })
            .joinToString(", ")
}