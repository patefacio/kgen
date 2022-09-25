package kgen.rust

interface AttrList : AsRust {
    val attrs: List<Attr>
    override val asRust: String
        get() = attrs.asRust
}