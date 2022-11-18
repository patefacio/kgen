package kgen.rust

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ConstTest {

    @Test
    fun getAsRust() {
        assertEquals(
            """
            /// This is a foo
            pub const FOO: i32 = 234;
            """.trimIndent(),
            Const("foo", "This is a foo", I32, 234).asRust
        )

        assertEquals(
            """
            /// This is a foo
            pub const FOO: &str = "Foo";
            """.trimIndent(),
            Const("foo", "This is a foo", StrRef(), "Foo").asRust
        )
    }
}