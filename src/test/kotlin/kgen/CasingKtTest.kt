package kgen

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CasingKtTest {

    @Test
    fun words() {
        assertEquals(listOf("this", "is", "cool"), words("this_is_cool"))
        assertEquals(listOf("this", "is", "cool"), words("thisIsCool"))
        assertEquals(listOf("this", "is", "cool"), words("ThisIsCool"))
        assertEquals(listOf("this", "is", "cool"), words("THIS_IS_COOL"))
    }

    @Test
    fun capCamel() {
        assertEquals("ThisIsCool", capCamel("this_is_cool"))
        assertEquals("ThisIsCool", capCamel("thisIsCool"))
        assertEquals("ThisIsCool", capCamel("ThisIsCool"))
        assertEquals("ThisIsCool", capCamel("This_Is_Cool"))
    }

    @Test
    fun camel() {
        assertEquals("thisIsCool", camel("this_is_cool"))
        assertEquals("thisIsCool", camel("thisIsCool"))
        assertEquals("thisIsCool", camel("ThisIsCool"))
        assertEquals("thisIsCool", camel("This_Is_Cool"))
    }

    @Test
    fun is_snake() {
        assertTrue(isSnake("this_is_snake_case"))
        assertFalse(isSnake("this_is_NOT_snake_case"))
    }

    @Test
    fun asSnake() {
        assertEquals(
            "foo_bar_goo",
            "FooBarGoo".asSnake
        )

        assertEquals(
            "foo_bar_goo",
            "fooBarGoo".asSnake
        )
    }
}