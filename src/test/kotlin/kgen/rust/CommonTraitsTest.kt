package kgen.rust

import kgen.noWhitespace
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CommonTraitsTest {

    @Test
    fun commonTraits()
    {
        assertEquals("""
impl Default for f64 {
  
  /// A trait for giving a type a useful default value.
  /// 
  ///   * _return_ - The new default instance
  fn default() -> Self {
    // α <fn Default::default for f64>
    todo!("Implement `default`")
    // ω <fn Default::default for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, defaultTrait).asRust.noWhitespace)

        assertEquals(
            """
impl Debug for f64 {
  
  /// Format the instance.l
  /// 
  ///   * **f** - The formatter
  ///   * _return_ - Formatted instance
  fn fmt(
    &self,
    #[allow(unused)] f: &mut Formatter<'_>
  ) -> ::core::fmt::Result {
    // α <fn Debug::fmt for f64>
    todo!("Implement `fmt`")
    // ω <fn Debug::fmt for f64>
  }
}
            """.trimIndent().noWhitespace,
           TraitImpl(F64, debugTrait).asRust.noWhitespace
        )

        assertEquals("""
impl PartialEq for f64 {
  
  /// Returns true if `other` equals `self`.
  /// 
  ///   * **other** - Instance to compare
  ///   * _return_ - True if `other` equals `self`.
  fn eq(
    &self,
    other: &Self
  ) -> bool {
    // α <fn PartialEq::eq for f64>
    todo!("Implement `eq`")
    // ω <fn PartialEq::eq for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, partialEqualTrait).asRust.noWhitespace)

        assertEquals("""
impl AddAssign for f64 {
  
  /// Add `Rhs` to `self`
  /// 
  ///   * **rhs** - Right hand side
  fn add_assign(
    & mut self,
    rhs: Rhs
  ) {
    // α <fn AddAssign::add_assign for f64>
    todo!("Implement `add_assign`")
    // ω <fn AddAssign::add_assign for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, addAssignTrait).asRust.noWhitespace)


        assertEquals("""
impl MulAssign for f64 {
  
  /// Multiply `self` by `Rhs`
  /// 
  ///   * **rhs** - Right hand side
  fn mul_assign(
    & mut self,
    rhs: Rhs
  ) {
    // α <fn MulAssign::mul_assign for f64>
    todo!("Implement `mul_assign`")
    // ω <fn MulAssign::mul_assign for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, mulAssignTrait).asRust.noWhitespace)

        assertEquals("""
impl Mul for f64 {
  
  /// Return new `self` multiplied by `Rhs`.
  /// 
  ///   * **rhs** - Right hand side
  ///   * _return_ - The new scaled `Self`
  fn mul(
    self,
    rhs: Rhs
  ) -> Self::Output {
    // α <fn Mul::mul for f64>
    todo!("Implement `mul`")
    // ω <fn Mul::mul for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, mulTrait).asRust.noWhitespace)

        assertEquals("""
impl Neg for f64 {
  
  /// Return negated `self`.
  /// 
  ///   * _return_ - The new negated `Self`
  fn neg(
    self
  ) -> Self::Output {
    // α <fn Neg::neg for f64>
    todo!("Implement `neg`")
    // ω <fn Neg::neg for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, negTrait).asRust.noWhitespace)

        assertEquals("""
impl Not for f64 {
  
  /// The unary logical negation operator !.
  /// 
  ///   * _return_ - The new negated `Self`
  fn not(
    self
  ) -> Self::Output {
    // α <fn Not::not for f64>
    todo!("Implement `not`")
    // ω <fn Not::not for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, notTrait).asRust.noWhitespace)

        assertEquals("""
impl Iterator for f64 {
  
  /// The next item in the sequence.
  /// 
  ///   * _return_ - The next item if available.
  fn next(
    & mut self
  ) -> Option<Self::Item> {
    // α <fn Iterator::next for f64>
    todo!("Implement `next`")
    // ω <fn Iterator::next for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, iteratorTrait).asRust.noWhitespace)

        assertEquals("""
impl IntoIterator for f64 {
  
  /// Consume object and providing to new iterator over its contents.
  /// 
  ///   * _return_ - The iterator.
  fn into_iter(
    self
  ) -> Self::IntoIter {
    // α <fn IntoIterator::into_iter for f64>
    todo!("Implement `into_iter`")
    // ω <fn IntoIterator::into_iter for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, intoIteratorTrait).asRust.noWhitespace)

        assertEquals("""
impl Display for f64 {
  
  /// Format the instance.
  /// 
  ///   * **f** - Formatter to push formatted item to.
  ///   * _return_ - Formatted instance
  fn fmt(
    &self,
    #[allow(unused)] f: &mut Formatter<'_>
  ) -> ::core::fmt::Result {
    // α <fn Display::fmt for f64>
    todo!("Implement `fmt`")
    // ω <fn Display::fmt for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, displayTrait).asRust.noWhitespace)

        assertEquals("""
impl Clone for f64 {
  
  /// Clone the instance
  /// 
  ///   * _return_ - The cloned instance
  fn clone(
    &self
  ) -> Self {
    // α <fn Clone::clone for f64>
    todo!("Implement `clone`")
    // ω <fn Clone::clone for f64>
  }
  
  /// Clone the instance from source
  /// 
  ///   * **source** - The source to clone.
  ///   * _return_ - The cloned instance
  fn clone_from(
    &self,
    source: &Self
  ) -> Self {
    // α <fn Clone::clone_from for f64>
    todo!("Implement `clone_from`")
    // ω <fn Clone::clone_from for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, cloneTrait).asRust.noWhitespace)

        assertEquals("""
impl Send for f64 {
  
  
}
        """.noWhitespace, TraitImpl(F64, sendTrait).asRust.noWhitespace)

        assertEquals("""
impl Sync for f64 {
  
  
}
        """.noWhitespace, TraitImpl(F64, syncTrait).asRust.noWhitespace)

        assertEquals("""
impl Drop for f64 {
  
  /// Custom code within the destructor
  fn drop(
    & mut self
  ) {
    // α <fn Drop::drop for f64>
    todo!("Implement `drop`")
    // ω <fn Drop::drop for f64>
  }
}
        """.noWhitespace, TraitImpl(F64, dropTrait).asRust.noWhitespace)

    }
}
