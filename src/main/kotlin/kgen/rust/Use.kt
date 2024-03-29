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

val List<String>.asAllowUnusedUses
    get() = this.map {
        Use(
            it,
            attrs = Attr.Words("allow", "unused_imports").asAttrList
        )
    }

fun pubUses(vararg pathNames: String, allowUnusedImports: Boolean = false) =
    pathNames.map { Use(it, visibility = Visibility.Pub, allowUnusedImports = allowUnusedImports) }

val String.asPubUse get() = Use(this, visibility = Visibility.Pub)
val List<String>.asPubUses get() = this.map { Use(it, visibility = Visibility.Pub) }.toSet()

val List<String>.asPubCrateUses get() = this.map { Use(it, visibility = Visibility.PubCrate) }.toSet()
val List<String>.asPubExportUses get() = this.map { Use(it, visibility = Visibility.PubExport) }.toSet()

val useHashMap = Use("std::collections::HashMap")
val useBTreeMap = Use("std::collections::BTreeMap")
val useCow = Use("std::borrow::Cow")
val useBox = Use("std::boxed::Box")
val useRc = Use("std::rc::Rc")
val useRef = Use("std::cell::Ref")
val useRefMut = Use("std::cell::RefMut")
val useRefCell = Use("std::cell::RefCell")
val useArc = Use("std::sync::Arc")
val useMutex = Use("std::sync::Mutex")
val useRange = Use("std::ops::Range")
val useRangeInclusive = Use("std::ops::RangeInclusive")
val useMap = Use("std::iter::Map")
val useEnumerate = Use("std::iter::Enumerate")
val useDynClone = Use("dyn_clone::DynClone")

val Use.asUnused get() = this.copy(allowUnusedImports = true)