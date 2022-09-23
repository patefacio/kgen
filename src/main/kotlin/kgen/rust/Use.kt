package kgen.rust

import kgen.joinNonEmpty
import kgen.trailingSpace

data class Use(
    val pathName: String,
    val visibility: Visibility = Visibility.None,
    override val attrs: List<Attr> = emptyList()
) : AsRust, AttrList {
    override val asRust: String
        get() = listOf(
            attrs.asRust,
            "${trailingSpace(visibility.asRust)}use $pathName"
        ).joinNonEmpty()
}