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

    private val numberedEnumFields
        get() = when {
            enumFields.all { it.number == null } -> enumFields
                .withIndex()
                .map { (i, enumField) -> enumField.copy(number = i) }

            enumFields.any { it.number == null } ->
                throw RuntimeException("Either *NO* enum fields or *ALL* have numbers")

            else -> enumFields
        }

    override val asProto: String
        get() = listOf(
            blockComment(doc),
            "enum ${id.capCamel} {",
            indent(numberedEnumFields.map { "${it.asProto};" }.joinToString("\n\n")),
            "}"
        ).joinToString("\n")
}