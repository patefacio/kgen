package kgen.rust

import kgen.Id
import kgen.id
import kgen.trailingSpace

data class FnParam(
    val nameId: String,
    val type: Type = RustString,
    val doc: String = "TODO Document Param($nameId)",
    val isMutable: Boolean = false
) : Identifiable(id(nameId)), AsRust {

    override val asRust: String
        get() = "${trailingSpace(mutable(isMutable))}${id.snakeCaseName}: ${type.asRust}"
}