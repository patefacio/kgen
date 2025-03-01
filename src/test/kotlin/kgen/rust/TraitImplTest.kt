package kgen.rust

import kgen.noWhitespace
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
            fnBodies = mapOf("do_foo" to "println!(\"foo\")")
        )

        assertEquals(
            """impl Foo for i32 {
  
  /// TODO: Document Fn(do_foo)
  fn do_foo() {
    println!("foo")
  }
  
  /// TODO: Document Fn(do_goo)
  fn do_goo() {
    // α <fn Foo::do_goo for i32>
    todo!("Implement `do_goo`")
    // ω <fn Foo::do_goo for i32>
  }
}""".noWhitespace,
            traitImpl.asRust.noWhitespace
        )
    }

    @Test
    fun patchGeneric() {

        assertEquals(
            """
impl AddAssign<Foo> for i32 {
  
  /// Add `Rhs` to `self`
  /// 
  ///   * **rhs** - Right hand side
  fn add_assign(
    & mut self,
    rhs: Foo
  ) {
    // α <fn AddAssign::add_assign for i32>
    todo!("Implement `add_assign`")
    // ω <fn AddAssign::add_assign for i32>
  }
}
            """.noWhitespace,
            TraitImpl(I32, addAssignTrait, genericArgSet = GenericArgSet("Foo".asType)).asRust.noWhitespace
        )
    }
}