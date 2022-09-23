package kgen.rust

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UseTest {
    @Test
    fun basics() {
        assertEquals(
            "use money",
            Use("money").asRust
        )
    }
}