package kgen.rust

open class StrRef(
    lifetime: Lifetime? = null,
    isMutable: Boolean = false
) : Ref(Str, lifetime, isMutable)