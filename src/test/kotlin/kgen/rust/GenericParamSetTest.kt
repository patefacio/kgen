package kgen.rust

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class GenericParamSetTest {

    @Test
    fun getAsRust() {

        assertEquals(
            "<'a, 'b, 'c>",
            GenericParamSet(lifetimes = listOf("a", "b", "c")).asRust
        )


        assertEquals(
            "<'a, 'b, t1, t2, t3>",
            GenericParamSet("t1", "t2", "t3", lifetimes = "a, b".asLifetimes).asRust
        )

        assertEquals(
            "<t1, t2, t3>",
            GenericParamSet("t1", "t2", "t3").asRust
        )
    }
}