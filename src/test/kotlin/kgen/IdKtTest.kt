package kgen

import kgen.rust.UnmodeledType
import kgen.rust.asType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

        assertEquals("this_is_a_test".asId, "This Is A Test".asId)
    }
}