package kgen.rust


data class GenericArgSet(
    val lifetimes: List<Lifetime> = emptyList(),
    val types: List<Type> = emptyList()
) : AsRust {
    val isEmpty get() = lifetimes.isEmpty() && types.isEmpty()

    constructor(vararg types: Type) : this(types = types.toList())
    constructor(vararg types: String) : this(types = types.map { it.asType })

    override val asRust: String
        get() = if (isEmpty) {
            ""
        } else {
            "<${(lifetimes.map { it.asRust } + types.map { it.asRust }).joinToString()}>"
        }
}