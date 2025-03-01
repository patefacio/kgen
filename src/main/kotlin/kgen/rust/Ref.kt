package kgen.rust

import kgen.trailingText

/**
 * Represents a Rust reference type that can be passed to a function.
 *
 * This class models a reference (`&T` or `&mut T`) in Rust, including optional lifetimes and mutability.
 *
 * @property referent The type being referred to by this reference.
 * @property lifetime The optional lifetime associated with the reference, defaulting to `null` if not specified.
 * @property isMutable Indicates whether the reference is mutable (`&mut`), defaulting to `false` for immutable references.
 */
open class Ref(
    val referent: Type,
    val lifetime: Lifetime? = null,
    val isMutable: Boolean = false
) : Type {

    // Internal utility to determine if the reference is mutable
    private val mut
        get() = mutable(isMutable)

    // Converts the optional lifetime into its Rust string representation
    private val rustLifetime get() = lifetime?.asRust ?: ""

    /**
     * Returns the Rust representation of this reference type, including its lifetime and mutability.
     * Example: `&'a mut T` or `&T`.
     */
    override val type
        get() = "&${trailingText(rustLifetime)}${trailingText(mut)}${referent.asRust}"

    /**
     * Indicates whether this type is a reference, always returning `true` for this class.
     */
    override val isRef: Boolean
        get() = true
}
