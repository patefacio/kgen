package kgen.proto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kgen.proto.FieldType.*

internal class FieldTypeTest {

    @Test
    fun basics() {
        assertEquals("string", ProtoString.asProto)
        assertEquals("float", ProtoFloat.asProto)
        assertEquals("int32", ProtoInt32.asProto)
        assertEquals("int64", ProtoInt64.asProto)
        assertEquals("uint32", ProtoUInt32.asProto)
        assertEquals("uint64", ProtoUInt64.asProto)
        assertEquals("sint32", ProtoSInt32.asProto)
        assertEquals("sint64", ProtoSInt64.asProto)
        assertEquals("fixed32", ProtoFixed32.asProto)
        assertEquals("sfixed32", ProtoSFixed32.asProto)
        assertEquals("sfixed64", ProtoSFixed64.asProto)
        assertEquals("bool", ProtoBoolean.asProto)



    }

}