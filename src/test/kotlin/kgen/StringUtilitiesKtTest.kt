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



}