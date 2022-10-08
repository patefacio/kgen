package kgen.proto

import kgen.Identifiable
import kgen.indent
import kgen.joinNonEmpty
import kgen.missingDoc

data class Message(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Proto Message"),
    val fields: List<Field> = emptyList(),
    val messages: List<Message> = emptyList(),
    val enums: List<Enum> = emptyList()
) : Identifiable(nameId), AsProto {

    constructor(
        nameId: String,
        doc: String,
        vararg fields: Field,
        messages: List<Message> = emptyList(),
        enums: List<Enum> = emptyList()
    ) : this(nameId, doc, fields.toList(), messages, enums)

    override val asProto: String
        get() = listOf(
            "message ${id.capCamel} {",
            indent(
                (fields + messages + enums).joinToString("\n\n") { "${it.asProto};" }),
            "}"
        ).joinToString("\n")
}