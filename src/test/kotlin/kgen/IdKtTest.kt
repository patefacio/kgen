package kgen

import kgen.rust.UnmodeledType
import kgen.rust.asType
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertFailsWith

internal class IdKtTest {

    @Test
    fun id() {
        assertEquals(
            "ValidSnakeName",
            id("valid_snake_name").capCamel
        )

        assertFailsWith<IllegalArgumentException> {
            id("ThisIsBadSinceNotSnake")
        }

        assertFailsWith<IllegalArgumentException> {
            id("this is also bad")
        }

        assertEquals(
            UnmodeledType("FooBar"),
            "FooBar".asType
        )
    }
}