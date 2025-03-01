package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FnBodyTest {

    @Test
    fun getAsRust() {

        assertEquals(
            """
            x: i32 = 42;
        """.trimIndent(),
            FnBody(
                """
                x: i32 = 42;
            """.trimIndent(),
            ).asRust
        )

        assertEquals(
            """
            /// This is the stuff
            x: i32 = 42;
            /// Nice
        """.trimIndent(),
            FnBody(
                """
                x: i32 = 42;
            """.trimIndent(),
                "/// This is the stuff",
                "/// Nice"
            ).asRust
        )


    }
}