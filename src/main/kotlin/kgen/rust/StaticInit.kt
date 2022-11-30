package kgen.rust

import kgen.*

data class StaticInit(
    val nameId: String,
    val doc: String,
    val type: Type,
    val value: String? = null,
    val attrs: AttrList = AttrList(),
    val visibility: Visibility = Visibility.Pub
) : Identifier(nameId), AsRust {

    override val asRust: String
        get() {
            val rustValue = when(value) {
                null -> "(|| {\n${emptyCloseDelimitedBlock("static_init for $nameId")}\n})()"
                else -> value
            }
            return listOf(
                commentTriple(doc),
                (attrs.attrs + attrDynamic).asOuterAttr,
                "${trailingText(visibility.asRust)}static ${id.shout}: ${type.asRust} = $rustValue;"
            ).joinNonEmpty()
        }

}