package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TypeImplTest {

    @Test
    fun getAsRust() {
        val s = Struct(
            "a",
            "An a",
            Field("x", "x value", I32),
            Field("y", "y value", I32)
        )
        val impl = TypeImpl(
            s,
            listOf(
                Fn("foo", "Do foo")
            )
        )

        assertEquals(
            """impl A {
  /// Do foo
  pub fn foo() {
    // α <fn A::foo>
    todo!("Implement `foo`")
    // ω <fn A::foo>
  }
}""",
            impl.asRust
        )
    }
}