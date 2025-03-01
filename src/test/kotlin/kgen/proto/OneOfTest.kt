package kgen.proto

import kgen.proto.FieldType.ProtoInt32
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OneOfTest {

    @Test
    fun getAsProto() {

        assertEquals(
            """
            /*
              `primary_color` supports _one_of_:
              - Red
              - Green
              - Blue
            */
            oneof primary_color {
              /*
                Red value
              */
              int32 red = 1;
              
              /*
                Green value
              */
              int32 green = 2;
              
              /*
                Red value
              */
              int32 blue = 3;
            }
        """.trimIndent(),
            OneOf(
                "primary_color",
                "parent",
                Field("red", ProtoInt32, "Red value"),
                Field("green", ProtoInt32, "Green value"),
                Field("blue", ProtoInt32, "Red value")
            ).autoNumbered.asProto
        )
    }
}