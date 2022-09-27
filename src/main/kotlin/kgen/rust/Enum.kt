package kgen.rust

data class Enum(
    val nameId: String
) : Identifiable(nameId), AsRust {
    override val asRust: String
        get() = TODO("Not yet implemented")
}