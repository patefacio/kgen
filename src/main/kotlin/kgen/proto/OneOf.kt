package kgen.proto

import kgen.Identifier
import kgen.asMarkdownList
import kgen.blockComment
import kgen.indent

data class OneOf(
    val nameId: String,
    val fields: List<Field>,
    val doc: String? = null
) : Identifier(nameId), Udt, AsProto, MessageField {

    override val isNumbered: Boolean
        get() = fields.all { it.number != null }

    override val numFields: Int
        get() = fields.size

    override fun copyFromNumber(number: Int) =
        copy(fields = fields.withIndex().map { (id, field) -> field.copy(number = number + id) })


    constructor(nameId: String, vararg fields: Field, doc: String? = null) : this(nameId, fields.toList(), doc)

    val autoNumbered
        get() = this.copy(
            fields = this.fields.withIndex().map { (i, field) -> field.copy(number = i + 1) }
        )

    override val asProto: String
        get() = listOf(
            blockComment("`$nameId` supports _one_of_:\n${fields.map { it.id.capCamel }.asMarkdownList}"),
            "oneof ${nameId} {",
            indent(fields.joinToString("\n\n") { "${it.asProto};" }),
            "}"
        ).joinToString("\n")
}
