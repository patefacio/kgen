package kgen.proto

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kgen.proto.FieldType.*

internal class FieldTest {

    @Test
    fun getAsProto() {

        assertEquals(
            """/*
  The foo bar field.
*/
int32 foo_bar = 1""",
            Field("foo_bar", ProtoInt32, "The foo bar field.", 1).asProto
        )

        assertEquals(
            """/*
  The id to its name.
*/
map<int64, string> id_to_name = 1""",
            Field(
                "id_to_name",
                FieldType.MapOf(ProtoInt64, ProtoString),
                "The id to its name.", 1
            ).asProto
        )

    }
}