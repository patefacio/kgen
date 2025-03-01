package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FieldTest {

    @Test
    fun toCode() {
        assertEquals(
            """
            /// TODO: Document Field(foo)
            pub foo: String
        """.trimIndent(),
            Field("foo").asRust
        )

        assertEquals(
            """
            /// The critical foo field
            pub foo: String
        """.trimIndent(),
            Field("foo", "The critical foo field").asRust
        )

        assertEquals(
            """
/// The critical foo field
#[foo(a="A", b="B", c="C")]
#[foo]
#[goo(a, b, c)]
#[foo="bar"]
pub foo: String
        """.trimIndent(),
            Field(
                "foo", "The critical foo field",
                attrs = AttrList(
                    Attr.Dict("foo", "c" to "C"),
                    Attr.Word("foo"),
                    Attr.Words("goo", "a", "b", "c"),
                    Attr.Dict("foo", "a" to "A", "b" to "B"),
                    Attr.Value("foo", "bar")
                )
            ).asRust
        )
    }
}