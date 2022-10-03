package kgen.rust


data class GenericArgSet(
    val lifetimes: List<Lifetime> = emptyList(),
    val types: List<Type> = emptyList()
) {
    val isEmpty get() = lifetimes.isEmpty() && types.isEmpty()
}