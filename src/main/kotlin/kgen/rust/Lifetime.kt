package kgen.rust

data class Lifetime(
    val lifetime: String
) : AsRust {
    override val asRust: String
        get() = "'${lifetime}"
}

val String.asLifetime get() = Lifetime(this)

val static = Lifetime("static")
