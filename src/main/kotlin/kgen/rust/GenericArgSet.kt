package kgen.rust


data class GenericArgSet(
    val lifetimes: List<Lifetime>,
    val types: List<Type>
)