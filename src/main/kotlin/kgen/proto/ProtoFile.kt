package kgen.proto

import kgen.*
import java.nio.file.Path
import kotlin.io.path.pathString

/** Model a protobuf file.
 *
 * @property nameId The name of the proto file (without .proto extension).
 * @property doc The comment for the file
 * @property packageName The package for the file
 * @property messages The list of messages in the proto file
 * @property enums The list of enums in the proto file
 * @property version The protobuf version
 */
data class ProtoFile(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Proto File"),
    val packageName: String = nameId,
    val messages: List<Message> = emptyList(),
    val enums: List<Enum> = emptyList(),
    val imports: List<String> = emptyList(),
    val version: Version = Version.Proto3,
) : Identifier(nameId), AsProto {

    val protoFileName get() = "$nameId.proto"

    override val asProto: String
        get() = listOf(
            "syntax = ${doubleQuote(version.asProto)};",
            imports.joinToString("\n") { "import ${doubleQuote(it)};" },
            "package $packageName;",
            enums.joinToString("\n\n") { it.asProto },
            messages.joinToString("\n\n") { it.asProto }
        ).joinNonEmpty("\n\n")

    fun generate(targetPath: Path) = checkWriteFile(
        targetPath.resolve(protoFileName).pathString,
        this.asProto
    )

    private fun recursiveAllOneOfs(message: Message): List<OneOf> = message
        .fields
        .filterIsInstance<OneOf>() + message.messages.map { recursiveAllOneOfs(it) }.flatten()

    val allOneOfs get() = messages.map { recursiveAllOneOfs(it) }.flatten()

    val allMessages get() = messages.map { it.allMessages }
}

val List<ProtoFile>.allMessages get() = this.map { it.allMessages.flatten() }.flatten()