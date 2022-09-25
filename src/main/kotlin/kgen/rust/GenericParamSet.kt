package kgen.rust

data class GenericParamSet(
    val lifetimes: List<Lifetime>,
    val typeParams: List<TypeParam>
) : AsRust {
    override val asRust: String
        get() = "<${
            (lifetimes.map { it.asRust } + typeParams.map {
                it.asRust
            }).joinToString(", ")
        } > "
}