package kgen.proto

import kgen.Identifier
import kgen.blockComment
import kgen.indent
import kgen.missingDoc
import kgen.rust.Trait

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
    val fields: List<MessageField> = emptyList(),
    val messages: List<Message> = emptyList(),
    val enums: List<Enum> = emptyList(),
    val implementedTraits: Set<Trait> = emptySet()
) : Identifier(nameId), Udt, AsProto {

    constructor(
        nameId: String,
        doc: String,
        vararg fields: MessageField,
        messages: List<Message> = emptyList(),
        enums: List<Enum> = emptyList(),
        implementedTraits: Set<Trait> = emptySet()
    ) : this(nameId, doc, fields.toList(), messages, enums, implementedTraits)


    private val numberedFields
        get() = when {
            fields.all { !it.isNumbered } -> {
                var number = 1
                fields.map { messageField ->
                    val updatedField = messageField.copyFromNumber(number)
                    number += messageField.numFields
                    updatedField
                }
            }

            fields.any { !it.isNumbered } -> {
                throw RuntimeException("Either *NO* fields or *ALL* fields have numbers -> $nameId")
            }

            else -> fields
        }

    override val asProto: String
        get() = listOf(
            blockComment(doc),
            "message ${id.capCamel} {",
            indent(
                (numberedFields + messages + enums).joinToString("\n\n") { "${it.asProto};" }),
            "}"
        ).joinToString("\n")


    val allMessages: List<Message>
        get() = listOf(this) +
                messages.map { it.allMessages }.flatten()
}
