package kgen.proto

import kgen.Identifiable
import kgen.blockComment
import kgen.joinNonEmpty
import kgen.missingDoc

data class Field(
    val nameId: String,
    val type: FieldType,
    val doc: String = missingDoc(nameId, "Message Field"),
    val number: Int
) : Identifiable(nameId), AsProto {

    override val asProto: String
        get() = joinNonEmpty(
            blockComment(doc),
            "${type.asProto} $nameId = $number"
        )
}