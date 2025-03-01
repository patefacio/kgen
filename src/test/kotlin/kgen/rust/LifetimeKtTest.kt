package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LifetimeKtTest {

    @Test
    fun lifetimes() {

        assertEquals(
            "<'a, 'b, 'c>",
            lifetimes("a", "b", "c").asRust
        )
    }
}