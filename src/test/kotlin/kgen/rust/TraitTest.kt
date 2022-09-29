package kgen.rust

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TraitTest {

    @Test
    fun getAsRust() {

        assertEquals(
            """
                /// Supports performing foo and bar operations
                trait FooBar {
                  /// foo operation
                  fn foo();
                  
                  /// bar operation
                  fn bar();
                }
            """.trimIndent(),
            Trait(
                "foo_bar",
                doc = """
                    Supports performing foo and bar operations
                """.trimIndent(),
                listOf(
                    Fn("foo", "foo operation"),
                    Fn("bar", "bar operation")
                )
            ).asRust
        )
    }
}