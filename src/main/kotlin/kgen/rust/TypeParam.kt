package kgen.rust

import kgen.Identifier
import kgen.asId

/** A rust type parameter for generics.
 * @param nameId Snake case name for type parameter
 * @param default A default type as in `f64` in  `trait T<V = f64>`.
 * @param bounds Bounds for the type param
 */
data class TypeParam(
    val nameId: String,
    val default: Type? = null,
    val bounds: Bounds = Bounds()
) : Identifier(nameId), AsRust {
    override val asRust: String
        get() = nameId.asId.capCamel

    val asRustGenericDecl: String
        get() = if (default == null) {
            id.capCamel
        } else {
            "${id.capCamel} = ${default.asRust}"
        }

    val boundsDecl
        get() = if (bounds.isEmpty()) {
            null
        } else {
            "${id.capCamel}: ${bounds.asRust}"
        }
}

val T = TypeParam("t")
val S = TypeParam("s")

val String.asTypeParam get() = TypeParam(this)
val String.asTypeParams get() = this.split(",").map { TypeParam(it.trim()) }

val TypeParam.asTypeParams get() = listOf(this)