package kgen.rust

import kgen.Identifier
import kgen.leadingText
import kgen.trailingText

data class TypeAlias(
    val nameId: String,
    val aliased: Type,
    val visibility: Visibility = Visibility.None,
    val genericParamSet: GenericParamSet = GenericParamSet()
) : Identifier(nameId), AsRust {
    override val asRust: String
        get() = "${trailingText(visibility.asRust)}type ${id.capCamel}${leadingText(genericParamSet.asRust)} = ${aliased.asRust};"
}

