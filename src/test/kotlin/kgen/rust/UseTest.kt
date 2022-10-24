package kgen.rust

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UseTest {
    @Test
    fun basics() {
        assertEquals(
            """
                #[foo(goo, moo)]
                pub(crate) use money;
            """.trimIndent(),
            Use(
                "money",
                visibility = Visibility.PubCrate,
                attrs = AttrList(Attr.Words("foo", "goo", "moo"))
            ).asRust
        )


        assertEquals(
            Use("foo::bar::Goo"),
            Use("foo::bar::Goo")
        )

        assertEquals(
            1,
            setOf(
                Use("foo::bar::Goo"),
                Use("foo::bar::Goo")
            ).size
        )
    }
}