package kgen.rust

import kgen.Identifier
import kgen.asId

data class TypeParam(
    val nameId: String,
    val default: Type? = null,
    val bounds: Bounds = Bounds()
) : Identifier(nameId), AsRust {
    override val asRust: String
        get() = nameId.asId.shoutAbbrev

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