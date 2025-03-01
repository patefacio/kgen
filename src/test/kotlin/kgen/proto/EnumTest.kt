package kgen.proto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EnumTest {

    @Test
    fun getAsProto() {
        assertEquals(
            """/*
  Choose among available colors
*/
enum ColorChoice {
  /*
    The color red
  */
  RED = 0;
  
  /*
    The color green
  */
  GREEN = 1;
  
  /*
    The color blue
  */
  BLUE = 2;
}""",
            colorEnum.asProto
        )
    }
}