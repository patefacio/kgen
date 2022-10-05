package kgen.rust

open class AttrList(val attrs: List<Attr> = emptyList()) : AsRust {

    constructor(vararg attrs: Attr) : this(attrs.toList())

    override val asRust: String
        get() = attrs.asRust
}

fun derive(vararg derives: String) = AttrList(
    Attr.Words("derive", *derives)
)

