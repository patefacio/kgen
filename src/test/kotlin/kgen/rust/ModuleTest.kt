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
// --- constants ---
////////////////////////////////////////////////////////////////////////////////////
/// This is a foo
const FOO: i32 = 234;

////////////////////////////////////////////////////////////////////////////////////
// --- traits ---
////////////////////////////////////////////////////////////////////////////////////
/// TODO: Document Trait(t1)
trait T1 {
  /// TODO: Document Fn(foo)
  fn foo();
}

////////////////////////////////////////////////////////////////////////////////////
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// S struct
struct S {
}

// α <mod-def m1>
// ω <mod-def m1>
""".trimIndent(),
            Module(
                "m1",
                visibility = Visibility.PubCrate,
                macroUses = listOf("serde_derive"),
                testMacroUses = listOf("tester"),
                consts = listOf(
                    Const("foo", "This is a foo", I32, 234)
                ),
                structs = listOf(
                    Struct("s", "S struct")
                ),
                traits = listOf(
                    Trait("t1", functions = listOf(Fn("foo")))
                ),
                uses = uses("crate::time_revalue::{Revalue, RevalueOn}")
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
                        Module("l1c1", moduleType = ModuleType.Inline, codeBlock = null)
                    )
                ),
                Module(
                    "l2", moduleType = ModuleType.Inline,
                    modules = listOf(
                        Module("l2c2", moduleType = ModuleType.Inline)
                    )
                ),
                Module(
                    "file"
                )
            )
        )

        assertEquals(
            """
/// TODO: Document Module(root)
mod root {
  ////////////////////////////////////////////////////////////////////////////////////
  // --- mod decls ---
  ////////////////////////////////////////////////////////////////////////////////////
  mod file;
  
  
  /// TODO: Document Module(l1)
  mod l1 {
    
    /// TODO: Document Module(l1c1)
    mod l1c1 {
      
    }
    
    // α <mod-def l1>
    // ω <mod-def l1>
  }
  
  /// TODO: Document Module(l2)
  mod l2 {
    
    /// TODO: Document Module(l2c2)
    mod l2c2 {
      // α <mod-def l2c2>
      // ω <mod-def l2c2>
    }
    
    // α <mod-def l2>
    // ω <mod-def l2>
  }
  
  // α <mod-def root>
  // ω <mod-def root>
}
        """.trimIndent(),
            rootModule.asRust
        )


    }


}