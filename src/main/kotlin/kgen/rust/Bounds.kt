package kgen.rust

data class Bounds(
    val lifetimes: List<Lifetime> = emptyList(),
    val traits: List<String> = emptyList()
)