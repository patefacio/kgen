package kgen.rust

import kgen.Identifiable

data class TypeParam(
    val nameId: String,
    val default: Type? = null,
    val bounds: Bounds = Bounds()
) : Identifiable(nameId), AsRust {
    override val asRust: String
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