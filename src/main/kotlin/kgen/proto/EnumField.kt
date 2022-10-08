package kgen.proto

import kgen.Identifiable
import kgen.blockComment
import kgen.missingDoc

data class EnumField(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Enum Field"),
    val number: Int
) : Identifiable(nameId), AsProto {
    override val asProto: String
        get() = listOf(
            blockComment(doc),
            "${id.shout} = $number"
        ).joinToString("\n")
}