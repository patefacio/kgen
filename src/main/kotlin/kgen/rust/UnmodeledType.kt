package kgen.rust

import kgen.Id

data class UnmodeledType(
    override val type: String,
    override val doc: String? = null
) : Type

val String.asType get() = UnmodeledType(this)
val Id.asType get() = UnmodeledType(this.capCamel)