package kgen.rust

import kgen.id
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AttrTest {
    @Test
    fun attrBasics() {
        assertEquals(
            "#[foo]",
            Attr.Word("foo").asRust
        )

        assertEquals(
            "#[macro_use(a, b)]",
            Attr.Words("macro_use", listOf("a", "b")).asRust
        )

        assertEquals(
            "#[macro_use(a, b)]",
            Attr.Words("macro_use", "a", "b").asRust
        )

        assertEquals(
            "#[foo = \"bar\"]",
            Attr.Value("foo", "bar").asRust
        )

        assertEquals(
            "#[foo(name = \"bar\", age = \"3\")]",
            Attr.Dict("foo", mapOf("name" to "bar", "age" to "3")).asRust
        )

        assertEquals(
            "#[foo(name = \"bar\", age = \"3\")]",
            Attr.Dict("foo", "name" to "bar", "age" to "3").asRust
        )
    }
}