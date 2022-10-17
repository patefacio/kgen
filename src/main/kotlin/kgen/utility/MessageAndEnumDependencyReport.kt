package kgen.utility

import kgen.Identifiable
import kgen.Markdown
import kgen.asMarkdownBlockQuote
import kgen.asMarkdownTable
import kgen.proto.Message
import kgen.proto.ProtoFile

data class MessageAndEnumDependencyReport(val protoFiles: List<ProtoFile>) {
    private val dependencies = MessageAndEnumDependencies(protoFiles)

    fun messageDependenciesAsMarkdown(message: Message): String {
        val allEncountered = mutableSetOf<String>()
        val bindings = dependencies.getBindings(message).map { list ->
            list.map {
                allEncountered.add(it.type.id.capCamel)
                it.toString()
            }
        }

        val messagesNotInTree = dependencies
            .allMessagesAndEnums
            .keys
            .toSortedSet() - allEncountered

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

    fun unusedDependenciesAsMarkdown(): String {
        val messagesDependedOn = mutableSetOf<Identifiable>()
        dependencies
            .allMessagesAndEnums
            .forEach { (_, message) ->
                when (message) {
                    is Message -> {
                        val bindings = dependencies.getBindings(message)

                        bindings.forEach {
                            it
                                .drop(1)
                                .forEach { fieldAndIdentifier ->
                                    messagesDependedOn.add(fieldAndIdentifier.type)
                                }
                        }
                    }
                }
            }

        return Markdown(
            "Unused Messages",
            "Messages that are not referenced by any `proto` in the set",
            body = listOf(
                "This might imply they are needed.",
                (
                        dependencies
                            .allMessagesAndEnums.map { it.value }.toSet() - messagesDependedOn
                        )
                    .map { it.id.capCamel }
                    .chunked(4)
                    .asMarkdownTable(addBlankHeader = true)
            ).joinToString("\n\n")
        ).asMarkdown
    }
}