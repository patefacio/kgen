package kgen.rust

import kgen.Identifier
import kgen.commentTriple
import kgen.leadingText
import kgen.trailingText

data class TypeAlias(
    val nameId: String,
    val aliased: Type,
    val visibility: Visibility = Visibility.None,
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val doc: String? = null
) : Identifier(nameId), AsRust {

    val asRustName get() = id.capCamel

    private val docComment get() = if(doc != null) {
        "${commentTriple(doc)}\n"
    } else {
        ""
    }

    override val asRust: String
        get() = "$docComment${trailingText(visibility.asRust)}type $asRustName${leadingText(genericParamSet.asRust)} = ${aliased.asRust};"
}

