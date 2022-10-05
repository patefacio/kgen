package kgen.rust

import kgen.joinNonEmpty
import kgen.trailingText

data class Use(
    val pathName: String,
    val visibility: Visibility = Visibility.None,
    val attrs: AttrList = AttrList()
) : AsRust {

    constructor(
        pathName: String,
        vararg attrs: Attr,
        visibility: Visibility = Visibility.None
    ) : this(pathName, visibility, AttrList(attrs.toList()))


    override val asRust: String
        get() = listOf(
            attrs.asRust,
            "${trailingText(visibility.asRust)}use $pathName;"
        ).joinNonEmpty()
}

fun uses(vararg pathNames: String) = pathNames.map { Use(it) }