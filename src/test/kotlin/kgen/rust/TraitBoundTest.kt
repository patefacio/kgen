package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TraitBoundTest {

    @Test
    fun unmodeledTrait() {
        assertEquals(
            "Debug",
            TraitBound.Unmodeled("Debug").asRust
        )
    }
}