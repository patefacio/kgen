package kgen.rust

data class AttrList(val attrs: List<Attr> = emptyList()) : AsRust {

    constructor(vararg attrs: Attr) : this(attrs.toList())

    operator fun plus(moreAttrs: AttrList) = AttrList(attrs + moreAttrs.attrs)

    operator fun plus(attr: Attr) = AttrList(attrs + listOf(attr))

    override val asRust: String
        get() = attrs.asRust
}

fun derive(vararg derives: String) = AttrList(
    Attr.Words("derive", *derives)
)

