package kgen.rust

import kgen.Id

data class UnmodeledType(override val type: String) : Type

val String.asType get() = UnmodeledType(this)
val Id.asType get() = UnmodeledType(this.capCamel)