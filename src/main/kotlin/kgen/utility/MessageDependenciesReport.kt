package kgen.utility

import kgen.Identifiable
import kgen.Markdown
import kgen.asMarkdownBlockQuote
import kgen.asMarkdownTable
import kgen.proto.*

/** Creates a report for a `Message` in a set of protobufs that details
 * all Message and Enums related to it transitively.
 */
data class MessageDependenciesReport(val protoFiles: List<ProtoFile>) {

    private val allMessagesAndEnums =
        protoFiles
            .map { it.messages.associateBy { it.id.capCamel }.entries }
            .flatten()
            .associate { it.key to it.value as Identifiable } + protoFiles
            .map { it.enums.associateBy { it.id.capCamel }.entries }
            .flatten()
            .associate { it.key to it.value as Identifiable }


    /** The *dependencies* for a message can be thought of as a set of paths to each other
     * Message/Enum.
     */
    data class FieldAndIdentifier(
        val field: Field?,
        val type: Identifiable
    ) {
        override fun toString() = "${field?.id?.snake ?: "^"}.${type.id.capCamel}"
    }

    /** A MessageField has a type, which if one modeled internally is either
     * in the same proto file or qualified with like "core_enums.AccountType"
     * or "core.YearValue". This
     *
     */
    private fun MessageField.unqualifiedTypeName() = when (this) {
        is Field -> {
            when (this.type) {
                is FieldType.MapOf -> (this.type as FieldType.MapOf).valueType.asProto
                else -> this.type.asProto
            }
                .split(".")
                .last()
        }

        else -> null
    }

    fun reportAsMarkdown(message: Message): String {
        val allEncountered = mutableSetOf<String>()
        val bindings = getBindings(message).map { list ->
            list.map {
                allEncountered.add(it.type.id.capCamel)
                it.toString()
            }
        }

        val messagesNotInTree = allMessagesAndEnums.keys.toSortedSet() - allEncountered

        return Markdown(
            "Dependencies For ${message.id.capCamel}",
            body = listOf(
                message.doc.asMarkdownBlockQuote,
                if (bindings.size == 1) {
                    "## Does not use *any* internally modeled items."
                } else {
                    listOf(
                        bindings.asMarkdownTable(addBlankHeader = true),
                        "---",
                        "## Not Encountered",
                        messagesNotInTree
                            .chunked(8)
                            .asMarkdownTable(addBlankHeader = true)
                    ).joinToString("\n\n")
                }

            ).joinToString("\n\n")
        ).asMarkdown
    }

    fun getBindings(message: Message) = getBindings(listOf(listOf(FieldAndIdentifier(null, message))))

    private fun getBindings(bindingsSoFar: List<List<FieldAndIdentifier>> = emptyList()): List<List<FieldAndIdentifier>> {
        val current = bindingsSoFar.last().last()
        val childMessages = when (val currentType = current.type) {
            is Message -> {
                currentType.fields.mapNotNull { field: MessageField ->
                    when (field) {
                        is OneOf -> field.fields.map {
                            FieldAndIdentifier(it, it)
                        }

                        else -> {
                            val type = allMessagesAndEnums[field.unqualifiedTypeName()]
                            if (type != null) {
                                listOf(FieldAndIdentifier(field as Field, type))
                            } else {
                                null
                            }
                        }
                    }
                }
            }

            else -> emptyList()
        }.flatten()


        return if (childMessages.isEmpty()) {
            bindingsSoFar
        } else {
            bindingsSoFar + childMessages.map {
                getBindings(listOf(bindingsSoFar.last() + it))
            }.flatten()
        }
    }
}
