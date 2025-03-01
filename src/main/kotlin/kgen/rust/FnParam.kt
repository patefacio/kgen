package kgen.rust

import kgen.Identifier
import kgen.trailingText

/** A rust function parameter.
 *
 * @property nameId Snake case name for the parameter
 * @property type The rust type of the parameter
 * @property doc The rust doc comment for the parameter
 * @property isMutable If true parameter is `mutable`
 * @property allowUnused If true `#[allow(unused)]` attribute associated with parameter
 * @property attrs Rust attributes associated with the parameter
 * @property uses Imports associated with type of parameter
 */
data class FnParam(
    val nameId: String,
    val type: Type = RustString,
    val doc: String = "TODO Document Param($nameId)",
    val isMutable: Boolean = false,
    val allowUnused: Boolean = false,
    val attrs: AttrList = AttrList(),
    val uses: Set<Use> = emptySet()
) : Identifier(nameId), AsRust {

    constructor(nameId: String, type: String, doc: String) : this(nameId, UnmodeledType(type), doc)

    // TODO: So ugly - using regex to back out special case
    private val selfWithLifetime = """\s*&\s*'(\w+)\s+Self""".toRegex()

    override val asRust: String
        get() = if (nameId == "self") {
            when (type.asRust) {
                "Box<Self>" -> "self: Box<Self>"
                "Arc<Self>" -> "self: Arc<Self>"
                "Self" -> if (isMutable) {
                    "mut self"
                } else {
                    "self"
                }
                "& Self" -> "&self"
                "& mut Self" -> "& mut self"
                "&'a Self" -> "& 'a self"
                "&'a mut Self" -> "& 'a mut self"
                else -> throw Exception("Invalid self var `${type.asRust}`.")
            }
        } else {
            val typeAsRust = type.asRust
            val lifetimeMatch = selfWithLifetime.find(typeAsRust)
            val attrs = if (allowUnused) {
                (attrs.attrs + attrAllowUnused).asAttrList
            } else {
                attrs
            }

            if (lifetimeMatch != null) {
                val lifetime = lifetimeMatch.groupValues[1]
                "& '$lifetime ${trailingText(mutable(isMutable))}${id.snakeCaseName}"
            } else {
                "${trailingText(mutable(isMutable))}${trailingText(attrs.asOuterAttr)}${id.snakeCaseName}: ${type.asRust}"
            }
        }
}

val self = FnParam("self", Self)
val selfMut = self.copy(isMutable = true)
val refSelf = FnParam("self", RefSelf)
fun refSelf(lifetime: String = "a") = FnParam("self", "&'$lifetime Self".asType)
val refMutSelf = FnParam("self", RefMutSelf)
fun refMutSelf(lifetime: String = "a") = FnParam("self", "&'$lifetime mut Self".asType)
