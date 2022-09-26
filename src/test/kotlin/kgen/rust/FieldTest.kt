package kgen.rust

import kgen.id
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class FieldTest {

    @Test
    fun toCode() {
        assertEquals(
            """
            /// TODO: DOCUMENT Field(foo)
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
            #[foo]
            #[goo(a, b, c)]
            #[foo(a = "A", b = "B")]
            #[foo = "bar"]
            pub foo: String
        """.trimIndent(),
            Field(
                "foo", "The critical foo field",
                attrs = AttrList(
                    Attr.Word("foo"),
                    Attr.Words("goo", "a", "b", "c"),
                    Attr.Dict("foo", "a" to "A", "b" to "B"),
                    Attr.Value("foo", "bar")
                )
            ).asRust
        )
    }
}