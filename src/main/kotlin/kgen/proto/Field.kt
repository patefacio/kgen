package kgen.proto

import kgen.*

data class Field(
    val nameId: String,
    val type: FieldType,
    val doc: String = missingDoc(nameId, "Message Field"),
    val number: Int,
    val repeated: Boolean = false,
    val optional: Boolean = false
) : Identifiable(nameId), AsProto {

    private val repeatedDecl = if(repeated) {
        "repeated "
    } else {
        ""
    }

    private val optionalDecl = if(optional) {
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