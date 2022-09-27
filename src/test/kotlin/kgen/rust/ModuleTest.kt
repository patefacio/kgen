package kgen.rust

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ModuleTest {

    @Test
    fun getAsRust() {

        assertEquals(
            """
//! TODO: Document Module(m1)

////////////////////////////////////////////////////////////////////////////////////
// --- macro-use imports ---
////////////////////////////////////////////////////////////////////////////////////
#[macro_use]
extern crate serde_derive;

////////////////////////////////////////////////////////////////////////////////////
// --- test-macro-use imports ---
////////////////////////////////////////////////////////////////////////////////////
#[cfg(test)]
#[macro_use]
extern crate tester;

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
use crate::time_revalue::{Revalue, RevalueOn};

////////////////////////////////////////////////////////////////////////////////////
// --- traits ---
////////////////////////////////////////////////////////////////////////////////////
/// TODO: Document Trait(t1)
trait T1 {
  /// TODO: Document foo
  fn foo() {
    // α <trait fn T1::foo>
    // ω <trait fn T1::foo>
  }
}

////////////////////////////////////////////////////////////////////////////////////
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// S struct
struct S {
}
""".trimIndent(),
            Module(
                "m1",
                visibility = Visibility.PubCrate,
                macroUses = listOf("serde_derive"),
                testMacroUses = listOf("tester"),
                structs = listOf(
                    Struct("s", "S struct")
                ),
                traits = listOf(
                    Trait("t1", functions = listOf(Fn("foo")))
                ),
                uses = listOf(Use("crate::time_revalue::{Revalue, RevalueOn}"))
            ).asRust
        )

    }

    @Test
    fun nesting() {
        val rootModule = Module(
            "root", moduleType = ModuleType.Inline,
            modules = listOf(
                Module(
                    "l1", moduleType = ModuleType.Inline,
                    modules = listOf(
                        Module("l1c1", moduleType = ModuleType.Inline)
                    )
                ),
                Module(
                    "l2", moduleType = ModuleType.Inline,
                    modules = listOf(
                        Module("l2c2", moduleType = ModuleType.Inline)
                    )
                )
            )
        )

        assertEquals(
            """
/// TODO: Document Module(root)
mod root; {
  /// TODO: Document Module(l1)
  mod l1; {
    /// TODO: Document Module(l1c1)
    mod l1c1; {
      
    }
  }
  
  /// TODO: Document Module(l2)
  mod l2; {
    /// TODO: Document Module(l2c2)
    mod l2c2; {
      
    }
  }
}
        """.trimIndent(),
            rootModule.asRust
        )


    }


}