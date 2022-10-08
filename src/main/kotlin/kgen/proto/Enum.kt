package kgen.proto

import kgen.Identifiable
import kgen.missingDoc

data class Enum(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Proto Enum"),
    val enumFields: List<EnumField>
) : Identifiable(nameId), AsProto {

    override val asProto: String
        get() = TODO("Not yet implemented")
}