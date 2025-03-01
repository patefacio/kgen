package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FnParamTest {

    @Test
    fun fnParam() {
        assertEquals(
            "foo: i32",
            FnParam("foo", I32).asRust
        )
    }
}