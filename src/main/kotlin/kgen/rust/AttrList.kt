package kgen.rust

/** Models a list of [Attr] values */
data class AttrList(val attrs: List<Attr> = emptyList()) : AsAttr {

    /** Construct [AttrList] from one or more [Attr] */
    constructor(vararg attrs: Attr) : this(attrs.toList())

    /** Add [moreAttrs] to [attrs] */
    operator fun plus(moreAttrs: AttrList) = AttrList(attrs + moreAttrs.attrs)

    /** Add single [Attr] to [attrs] */
    operator fun plus(attr: Attr) = AttrList(attrs + listOf(attr))

    /** The attribute list as an _outer attribute_ */
    override val asOuterAttr: String
        get() = attrs.asOuterAttr

    /** The attribute list as an _inner attribute_ */
    override val asInnerAttr: String
        get() = attrs.asInnerAttr

}

fun derive(vararg derives: String) = AttrList(
    Attr.Words("derive", *derives)
)

fun commonSimpleEnumAttrs() = derive("Debug", "Copy", "Clone")
val commonDerives = derive("Debug", "Clone")
val clapSubcommand = Attr.Words("command", "subcommand")

