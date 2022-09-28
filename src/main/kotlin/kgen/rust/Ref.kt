package kgen.rust

import kgen.trailingText

open class Ref(
    val referent: Type,
    val lifetime: Lifetime? = null,
    val isMutable: Boolean = false
) : Type {

    private val mut
        get() = mutable(isMutable)

    private val rustLifetime get() = lifetime?.asRust ?: ""

    override val type
        get() = "&${trailingText(rustLifetime)}${
            trailingText(mut)
        }${referent.asRust}"
    override val isRef: Boolean
        get() = true
}

