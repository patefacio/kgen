package kgen.proto

import kgen.proto.FieldType.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
                MapOf(ProtoInt64, ProtoString),
                "The id to its name.", 1
            ).asProto
        )

    }
}