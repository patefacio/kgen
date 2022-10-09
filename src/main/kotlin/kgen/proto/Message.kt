package kgen.proto

import kgen.*

/** A modeled message.
 *
 * @property nameId The name of the message.
 * @property doc The documentation for the message.
 * @property fields List of fields in the message.
 * @property messages List of (sub)messages in the message.
 * @property enums List of enums in the message.
 */
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
            blockComment(doc),
            "message ${id.capCamel} {",
            indent(
                (fields + messages + enums).joinToString("\n\n") { "${it.asProto};" }),
            "}"
        ).joinToString("\n")
}