package kgen.rust

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AssociatedTypeTest {
    @Test
    fun generalUse() {
        assertEquals(
            """
                /// This is an associated type
                type FooBar;
            """.trimIndent(),
            AssociatedType("foo_bar", "This is an associated type").asRust
        )
    }
}