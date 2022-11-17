package kgen.rust

import kgen.joinNonEmpty
import kgen.trailingText

data class Use(
    val pathName: String,
    val visibility: Visibility = Visibility.None,
    val attrs: AttrList = AttrList(),
    val allowUnusedImports: Boolean = false
) : AsRust {

    constructor(
        pathName: String,
        vararg attrs: Attr,
        visibility: Visibility = Visibility.None
    ) : this(pathName, visibility, AttrList(attrs.toList()))


    override val asRust: String
        get() = listOfNotNull(
            if (allowUnusedImports) {
                "#[allow(unused_imports)]"
            } else {
                null
            },
            attrs.asOuterAttr,
            "${trailingText(visibility.asRust)}use $pathName;"
        ).joinNonEmpty()
}

fun uses(vararg pathNames: String) = pathNames.map { Use(it) }.toSet()

val List<String>.asUses get() = this.map { Use(it) }.toSet()

fun pubUses(vararg pathNames: String) = pathNames.map { Use(it, visibility = Visibility.Pub) }