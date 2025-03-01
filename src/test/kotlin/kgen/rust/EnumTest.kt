package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EnumTest {

    @Test
    fun getAsRust() {
        assertEquals(
            """
/// TODO: Document Enum(message)
pub enum Message {
  /// Means quit the process
  Quit,
  /// Request to change color
  ChangeColor(i32, i32, i32),
  /// Request to move object
  Move {
    /// TODO: Document Field(x)
    x: i32,
    /// TODO: Document Field(y)
    y: i32
  },
  /// Request to write object
  Write(String)
}
""".trimIndent(),

            Enum(
                "message",
                variants = listOf(
                    Variant.UnitStruct("quit", "Means quit the process"),
                    Variant.TupleStruct("change_color", "Request to change color", I32, I32, I32),
                    Variant.Struct(
                        "move",
                        "Request to move object",
                        Field("x", type = I32), Field("y", type = I32)
                    ),
                    Variant.TupleStruct("write", "Request to write object", RustString)
                )
            ).asRust
        )

        assertEquals(
            """
/// A basic message
pub enum Message {
  /// TODO: Document Enum.UnitStruct(quit)
  Quit,
  /// Request to change color
  ChangeColor(i32, i32, i32),
  /// Request to move object
  Move {
    /// TODO: Document Field(x)
    x: i32,
    /// TODO: Document Field(y)
    y: i32
  },
  /// Request to write object
  Write(String)
}
""".trimIndent(),

            Enum(
                "message", "A basic message",
                Variant.UnitStruct("quit"),
                Variant.TupleStruct("change_color", "Request to change color", I32, I32, I32),
                Variant.Struct(
                    "move",
                    "Request to move object",
                    Field("x", type = I32), Field("y", type = I32)
                ),
                Variant.TupleStruct("write", "Request to write object", RustString)
            ).asRust
        )
    }
}