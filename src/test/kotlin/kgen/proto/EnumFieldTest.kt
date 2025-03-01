package kgen.proto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EnumFieldTest {

    @Test
    fun getAsProto() {
        assertEquals(
            """/*
  The red value.
*/
RED_VALUE = 42""",
            EnumField("red_value", "The red value.", 42).asProto
        )
    }
}