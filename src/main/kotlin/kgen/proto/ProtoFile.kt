package kgen.proto

import kgen.Identifiable
import kgen.missingDoc

data class ProtoFile(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Proto File"),
    val packageName: String = nameId,
    val messages: List<Message> = emptyList(),
    val version: Version = Version.Proto3
) : Identifiable(nameId){
}