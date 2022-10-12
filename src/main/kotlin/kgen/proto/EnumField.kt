package kgen.proto

import kgen.Identifier
import kgen.blockComment
import kgen.missingDoc

data class EnumField(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Enum Field"),
    val number: Int? = null
) : Identifier(nameId), AsProto {
    override val asProto: String
        get() = listOf(
            blockComment(doc),
            "${id.shout} = $number"
        ).joinToString("\n")
}