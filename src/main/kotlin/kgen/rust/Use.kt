package kgen.rust

import kgen.asId
import kgen.joinNonEmpty
import kgen.trailingText

data class Use(
    val pathName: String,
    val visibility: Visibility = Visibility.None,
    val attrs: AttrList = AttrList(),
    val allowUnusedImports: Boolean = false,
    val aliasNameId: String? = null
) : AsRust {

    constructor(
        pathName: String,
        vararg attrs: Attr,
        visibility: Visibility = Visibility.None,
        allowUnusedImports: Boolean = false,
        aliasNameId: String? = null
    ) : this(pathName, visibility, AttrList(attrs.toList()), allowUnusedImports, aliasNameId)


    private val asAlias get() = if(aliasNameId != null) {
        " as ${aliasNameId.asId.capCamel}"
    } else {
        ""
    }

    override val asRust: String
        get() = listOfNotNull(
            if (allowUnusedImports) {
                "#[allow(unused_imports)]"
            } else {
                null
            },
            attrs.asOuterAttr,
            "${trailingText(visibility.asRust)}use $pathName$asAlias;"
        ).joinNonEmpty()
}

fun uses(vararg pathNames: String) = pathNames.map { Use(it) }.toSet()

val List<String>.asUses get() = this.map { Use(it) }.toSet()

fun pubUses(vararg pathNames: String) = pathNames.map { Use(it, visibility = Visibility.Pub) }

val List<String>.asPubUses get() = this.map { Use(it, visibility = kgen.rust.Visibility.Pub) }.toSet()