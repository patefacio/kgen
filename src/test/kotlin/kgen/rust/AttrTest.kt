package kgen.rust

import kgen.id
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AttrTest {
    @Test
    fun attrBasics() {
        assertEquals(
            "#[foo]",
            Attr.Word("foo").asOuterAttr
        )

        assertEquals(
            "#[macro_use(a, b)]",
            Attr.Words("macro_use", "a", "b").asOuterAttr
        )

        assertEquals(
            "#[foo=\"bar\"]",
            Attr.Value("foo", "bar").asOuterAttr
        )

        assertEquals(
            "#[foo(name=\"bar\", age=\"3\")]",
            Attr.Dict("foo", "name" to "bar", "age" to "3").asOuterAttr
        )
    }
}