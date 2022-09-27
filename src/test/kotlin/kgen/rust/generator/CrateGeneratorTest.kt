package kgen.rust.generator

import kgen.rust.Crate
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CrateGeneratorTest {

    @Test
    fun getSrcPath() {
        assertEquals(
            "/foo/bar/goo/src",
            CrateGenerator(Crate("foo"), "/foo/bar/goo").srcPathString
        )
    }
}