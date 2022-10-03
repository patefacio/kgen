package kgen.rust

import kgen.trailingText

data class TypeAlias(
    val nameId: String,
    val aliased: Type,
    val visibility: Visibility = Visibility.None
) : Identifiable(nameId), AsRust {
    override val asRust: String
        get() = "${trailingText(visibility.asRust)}type ${id.capCamel} = ${aliased.asRust};"
}

