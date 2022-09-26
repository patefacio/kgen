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
                  fn foo() {
                    // α <trait fn FooBar::foo>
                    // ω <trait fn FooBar::foo>
                  }
                  
                  /// bar operation
                  fn bar() {
                    // α <trait fn FooBar::bar>
                    // ω <trait fn FooBar::bar>
                  }
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