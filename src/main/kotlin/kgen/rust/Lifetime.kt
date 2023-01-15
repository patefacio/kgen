package kgen.rust

data class Lifetime(
    val lifetime: String
) : AsRust {
    override val asRust: String
        get() = "'${lifetime}"
}

val String.asLifetime get() = Lifetime(this)

val String.asLifetimes get() = this.split(",").map { Lifetime(it.trim()) }

fun lifetimes(vararg lifetime: String) = lifetime.map {
    Lifetime(
        if (it.startsWith("'")) {
            it.substring(1)
        } else {
            it
        }
    )
}

val List<Lifetime>.asRust
    get() = if (this.isNotEmpty()) {
        "<${this.joinToString(", ") { it.asRust }}>"
    } else {
        ""
    }

val static = Lifetime("static")
