package kgen.rust

import kgen.joinNonEmpty
import kgen.trailingSpace

data class Use(
    val pathName: String,
    val visibility: Visibility = Visibility.None,
    val attrs: AttrList = AttrList()
) : AsRust {
    override val asRust: String
        get() = listOf(
            attrs.asRust,
            "${trailingSpace(visibility.asRust)}use $pathName"
        ).joinNonEmpty()
}