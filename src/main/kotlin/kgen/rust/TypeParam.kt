package kgen.rust

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

    val boundsDecl
        get() = if (bounds.isEmpty()) {
            null
        } else {
            "$name: ${bounds.asRust}"
        }
}

val T = TypeParam("T")
val S = TypeParam("S")