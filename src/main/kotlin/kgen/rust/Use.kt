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

    private val asAlias
        get() = when {
            aliasNameId == null -> ""
            aliasNameId.all { it == '_' || it.isUpperCase() } -> " as $aliasNameId"
            else -> " as ${aliasNameId.asId.capCamel}"
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

val Use.asUses get() = setOf(this)
val String.asUses get() = listOf(this).asUses

val String.asAllowUnusedUses
    get() = listOf(
        Use(
            this,
            attrs = Attr.Words("allow", "unused_imports").asAttrList
        )
    )

fun pubUses(vararg pathNames: String) = pathNames.map { Use(it, visibility = Visibility.Pub) }

val List<String>.asPubUses get() = this.map { Use(it, visibility = kgen.rust.Visibility.Pub) }.toSet()

val useHashMap = Use("std::collections::HashMap")
val useCow = Use("std::borrow::Cow")
val useBox = Use("std::boxed::Box")
val useArc = Use("std::sync::Arc")
val useRange = Use("std::ops::Range")
val useMap = Use("std::iter::Map")
val useEnumerate = Use("std::iter::Enumerate")
val useDynClone = Use("dyn_clone::DynClone")