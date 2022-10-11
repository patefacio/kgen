package kgen.proto

import kgen.Identifier
import kgen.blockComment
import kgen.indent
import kgen.missingDoc

data class Enum(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Proto Enum"),
    val enumFields: List<EnumField>
) : Identifier(nameId), AsProto {


    constructor(nameId: String, doc: String, vararg enumFields: EnumField) :
            this(nameId, doc, enumFields.toList())

    override val asProto: String
        get() = listOf(
            blockComment(doc),
            "enum ${id.capCamel} {",
            indent(enumFields.map { "${it.asProto};" }.joinToString("\n\n")),
            "}"
        ).joinToString("\n")
}