package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FnTest {

    @Test
    fun getAsRust() {

        assertEquals(
            """
/// Does foo and bar
/// 
///   * **a** - The a value
///   * **b** - The b value
#[foo]
#[inline="always"]
pub fn foo_bar(
  a: i32,
  b: i32
) {
  a = a + 1;
}
""".trimIndent(),
            Fn(
                "foo_bar",
                "Does foo and bar",
                listOf(
                    FnParam("a", I32, "The a value"),
                    FnParam("b", I32, "The b value")
                ),

                body = FnBody(
                    "a = a + 1;"
                ),
                inlineDecl = InlineDecl.InlineAlways,
                attrs = AttrList(Attr.Word("foo"))
            ).asRust
        )

        assertEquals(
            """
            /// Does foo and bar
            pub fn foo_bar() {
              // α <fn foo_bar>
              todo!("Implement `foo_bar`")
              // ω <fn foo_bar>
            }
        """.trimIndent(),
            Fn(
                "foo_bar",
                "Does foo and bar"
            ).asRust

        )

    }

    @Test
    fun whereClause() {
        assertEquals(
            """
pub fn foo<T>()
where
  T: Debug + Addable""".trimIndent(),
            Fn(
                "foo", genericParamSet = GenericParamSet(
                    TypeParam(
                        "t",
                        bounds = Bounds("Debug", "Addable")
                    )
                )
            ).signature
        )
    }
}