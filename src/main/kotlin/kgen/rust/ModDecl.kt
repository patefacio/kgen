package kgen.rust

import kgen.Identifier
import kgen.joinNonEmpty
import kgen.trailingText

data class ModDecl(
    val nameId: String,
    val visibility: Visibility = Visibility.Pub,
    val attrs: AttrList = AttrList(),
) : Identifier(nameId) {
    val asModuleDecl: String
        get() = listOf(
            attrs.asOuterAttr,
            "${trailingText(visibility.asRust)}mod ${id.snake};"
        ).joinNonEmpty()

    val asModuleDef
        get() = listOf(
            attrs.asOuterAttr,
            "${trailingText(visibility.asRust)}mod ${id.snake}"
        ).joinNonEmpty()
}