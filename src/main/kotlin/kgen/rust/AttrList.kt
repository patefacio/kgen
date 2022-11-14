package kgen.rust

data class AttrList(val attrs: List<Attr> = emptyList()) : AsAttr {

    constructor(vararg attrs: Attr) : this(attrs.toList())

    operator fun plus(moreAttrs: AttrList) = AttrList(attrs + moreAttrs.attrs)

    operator fun plus(attr: Attr) = AttrList(attrs + listOf(attr))

    override val asOuterAttr: String
        get() = attrs.asOuterAttr

    override val asInnerAttr: String
        get() = attrs.asInnerAttr
}

fun derive(vararg derives: String) = AttrList(
    Attr.Words("derive", *derives)
)

