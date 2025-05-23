package kgen.proto

import kgen.proto.FieldType.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FieldTypeTest {

    @Test
    fun basics() {
        assertEquals("string", ProtoString.asProto)
        assertEquals("float", ProtoFloat.asProto)
        assertEquals("double", ProtoDouble.asProto)
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