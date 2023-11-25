package kgen.rust

data class Lifetime(
    val lifetime: String
) : AsRust {
    override val asRust: String
        get() = "'${lifetime}"
}

val String.asLifetime get() = Lifetime(this)

fun lifetime(lifetime: String) = Lifetime(lifetime.removePrefix("'").trim())

val String.asLifetimes get() = this.split(",").map { lifetime(it) }

fun lifetimes(vararg lifetime: String) = lifetime.map {
    lifetime(it)
}

val List<Lifetime>.asRust
    get() = if (this.isNotEmpty()) {
        "<${this.joinToString(", ") { it.asRust }}>"
    } else {
        ""
    }

val static = Lifetime("static")
