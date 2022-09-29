package kgen

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StringUtilitiesKtTest {

    @Test
    fun indent() {
        assertEquals(
            """
            // this is a test
            // of the emergency broadcast system
        """.trimIndent(),
            indent(
                """
                this is a test
                of the emergency broadcast system
            """.trimIndent(), indent = "// "
            )
        )

        assertEquals(
            """  this is a test
  of the emergency broadcast system""",
            indent(
                """this is a test
of the emergency broadcast system"""
            )
        )
    }

    @Test
    fun removeme() {

        fun foo(a: String, b: Int, vararg c: String, d: Int = 0, e: Int = 4) {

        }

        foo("a", 32, "a", "b", d = 3)

    }
}