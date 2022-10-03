package kgen.rust

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TraitImplTest {

    @Test
    fun getAsRust() {
        val trait = Trait(
            "foo", "A foo",
            Fn("do_foo"),
            Fn("do_goo"),
        )
        val traitImpl = TraitImpl(
            I32,
            trait,
            "Foo",
            bodies = mapOf("do_foo" to "println!(\"foo\")")
        )

        assertEquals(
            """impl Foo for i32 {
  /// TODO: Document Fn(do_foo)
  fn do_foo() {
    println!("foo")
  }
  
  /// TODO: Document Fn(do_goo)
  fn do_goo() {
    // α <do_goo>
    // ω <do_goo>
  }
}""",
            traitImpl.asRust
        )
    }
}