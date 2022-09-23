package kgen.rust

import kgen.trailingSpace

open class Ref(
    val referent: Type,
    val lifetime: Lifetime? = null,
    val isMutable: Boolean = false
) : Type {

    private val mut
        get() = if (isMutable) {
            "mut"
        } else {
            ""
        }
    private val rustLifetime get() = lifetime?.asRust ?: ""

    override val type
        get() = "&${trailingSpace(rustLifetime)}${
            trailingSpace(mut)
        }${referent.asRust}"
    override val isRef: Boolean
        get() = true
}

class StrRef(
    lifetime: Lifetime? = null,
    isMutable: Boolean = false
) : Ref(Str, lifetime, isMutable)