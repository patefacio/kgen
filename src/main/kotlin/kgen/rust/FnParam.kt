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

    override val asRust: String
        get() = "${trailingText(mutable(isMutable))}${id.snakeCaseName}: ${type.asRust}"
}

val self = FnParam("self", Self)
val refSelf = FnParam("self", RefSelf)
fun refSelf(lifetime: String) = FnParam("self", "&'$lifetime Self".asType)
val refMutSelf = FnParam("self", RefMutSelf)