package kgen.rust

import kgen.Id

data class UnmodeledType(
    val baseType: String,
    override val doc: String? = null,
    val genericParamSet: GenericParamSet = GenericParamSet()
) : Type {
    override val type: String
        get() = "${baseType}${genericParamSet.asRust}"
}

val String.asType get() = UnmodeledType(this)
val Id.asType get() = UnmodeledType(this.capCamel)

fun String.asGenericType(genericParamSet: GenericParamSet, doc: String? = null) =
    UnmodeledType(this, doc, genericParamSet)