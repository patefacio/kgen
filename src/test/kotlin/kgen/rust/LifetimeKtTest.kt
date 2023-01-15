package kgen.rust

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class LifetimeKtTest {

    @Test
    fun lifetimes() {

        assertEquals(
            "<'a, 'b, 'c>",
            lifetimes("a", "b", "c").asRust
        )
    }
}