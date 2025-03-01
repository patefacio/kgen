package kgen.proto

import kgen.Identifier
import kgen.blockComment
import kgen.joinNonEmpty
import kgen.missingDoc

data class Field(
    val nameId: String,
    val type: FieldType,
    val doc: String = missingDoc(nameId, "Message Field"),
    val number: Int? = null,
    val isRepeated: Boolean = false,
    val optionalJustification: String? = null,
    val requiredJustification: String? = null
) : Identifier(nameId), AsProto, MessageField {

    init {
        if (requiredJustification != null && optionalJustification != null) {
            throw Exception("Cannot be both `optional` and `required` due to $requiredJustification!")
        }
    }

    override val isNumbered: Boolean
        get() = number != null

    override val numFields: Int
        get() = 1

    override fun copyFromNumber(number: Int) = if (number == this.number) {
        this
    } else {
        copy(number = number)
    }

    private val repeatedDecl = if (isRepeated) {
        "repeated "
    } else {
        ""
    }

    val isOptional get() = optionalJustification != null

    private val optionalDecl = if (isOptional) {
        "optional "
    } else {
        ""
    }
    override val asProto: String
        get() = joinNonEmpty(
            blockComment(doc),
            "$repeatedDecl$optionalDecl${type.asProto} $nameId = $number"
        )
}