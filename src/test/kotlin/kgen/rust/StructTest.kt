package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StructTest {

    @Test
    fun getAsRust() {

        assertEquals(
            """
                /// A simple struct
                pub struct SimpleStruct<T> {
                  /// An a
                  pub a: i32,
                  /// A T
                  pub t: T
                }
            """.trimIndent(),
            Struct(
                "simple_struct", "A simple struct",
                listOf(
                    Field("a", "An a", I32),
                    Field("t", "A T", UnmodeledType("T"))
                ),
                visibility = Visibility.Pub,
                genericParamSet = GenericParamSet("t")
            ).asRust
        )

    }
}