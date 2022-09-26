package kgen.rust

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TraitBoundTest {

    @Test
    fun unmodeledTrait() {
        assertEquals(
            "Debug",
            TraitBound.Unmodeled("Debug").asRust
        )
    }
}