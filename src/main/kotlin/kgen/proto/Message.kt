package kgen.proto

import kgen.Identifiable
import kgen.joinNonEmpty
import kgen.missingDoc

data class Message(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Proto Message"),
    val fields: List<Field> = emptyList(),
    val messages: List<Message> = emptyList()
) : Identifiable(nameId), AsProto {

    override val asProto: String
        get() = listOf(

            "message ${id.capCamel} {",

            "}"
        ).joinNonEmpty()
}