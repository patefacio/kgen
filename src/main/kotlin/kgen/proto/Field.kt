package kgen.proto

import kgen.*

data class Field(
    val nameId: String,
    val type: FieldType,
    val doc: String = missingDoc(nameId, "Message Field"),
    val number: Int? = null,
    val repeated: Boolean = false,
    val optional: Boolean = false
) : Identifier(nameId), AsProto, MessageField {

    override val isNumbered: Boolean
        get() = number != null

    override val numFields: Int
        get() = 1

    override fun copyFromNumber(fromNumber: Int) = if (fromNumber == this.number) {
        this
    } else {
        copy(number = fromNumber)
    }

    private val repeatedDecl = if (repeated) {
        "repeated "
    } else {
        ""
    }

    private val optionalDecl = if (optional) {
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