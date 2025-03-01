package kgen.rust

/**
 * Represents a Rust string reference (`&str` or `&mut str`), optionally with a lifetime and mutability.
 *
 * This class is a specialized version of [Ref], where the `referent` is always set to `Str`,
 * modeling Rust's string references.
 *
 * @property lifetime The optional lifetime associated with the string reference, defaulting to `null` if not specified.
 * @property isMutable Indicates whether the string reference is mutable (`&mut str`), defaulting to `false` for immutable references.
 */
open class StrRef(
    lifetime: Lifetime? = null,
    isMutable: Boolean = false
) : Ref(Str, lifetime, isMutable)
