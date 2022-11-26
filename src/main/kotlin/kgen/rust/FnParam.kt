package kgen.rust

import kgen.Identifier
import kgen.trailingText

data class FnParam(
    val nameId: String,
    val type: Type = RustString,
    val doc: String = "TODO Document Param($nameId)",
    val isMutable: Boolean = false
) : Identifier(nameId), AsRust {


    constructor(nameId: String, type: String, doc: String) : this(nameId, UnmodeledType(type), doc)

    // TODO: So ugly - using regex to back out special case
    private val selfWithLifetime = """\s*&\s*'(\w+)\s+Self""".toRegex()

    override val asRust: String
        get() = when (type.asRust) {
            "Self" -> "self"
            "& Self" -> "&self"
            "& mut Self" -> "& mut self"
            else -> {
                val typeAsRust = type.asRust
                val lifetimeMatch = selfWithLifetime.find(typeAsRust)

                if(lifetimeMatch != null) {
                    val lifetime = lifetimeMatch.groupValues[1]
                    "& '$lifetime ${id.snakeCaseName}"
                } else {
                    "${trailingText(mutable(isMutable))}${id.snakeCaseName}: ${type.asRust}"
                }
            }
        }
}

val self = FnParam("self", Self)
val refSelf = FnParam("self", RefSelf)
fun refSelf(lifetime: String = "a") = FnParam("self", "&'$lifetime Self".asType)
val refMutSelf = FnParam("self", RefMutSelf)
fun refMutSelf(lifetime: String = "a") = FnParam("self", "&'$lifetime mut Self".asType)
