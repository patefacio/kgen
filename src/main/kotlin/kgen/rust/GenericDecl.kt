package kgen.rust


data class Bounds(
    val lifetimes: List<Lifetime> = emptyList(),
    val traits: List<String> = emptyList()
)

data class TypeParam(
    val name: String,
    val default: Type? = null,
    val bounds: Bounds = Bounds()
) : AsRust {
    override val asRust: String
        get() = if (default == null) {
            name
        } else {
            "$name = ${default.asRust}"
        }
}

val T = TypeParam("T")
val S = TypeParam("S")

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

data class GenericArgSet(
    val lifetimes: List<Lifetime>,
    val types: List<Type>
)