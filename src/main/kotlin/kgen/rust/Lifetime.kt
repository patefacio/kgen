package kgen.rust

/** Models the lifetime of an item
 * @property lifetime The lifetime as a string
 */
data class Lifetime(
    val lifetime: String
) : AsRust {
    override val asRust: String
        get() = "'${lifetime}"
}

/** Convenience extension converting string to lifetime */
val String.asLifetime get() = Lifetime(this)

/** Convenience function converting [lifetime] to a lifetime, ensuring
 * it does not already have the '`'
 */
fun lifetime(lifetime: String) = Lifetime(lifetime.removePrefix("'").trim())

/** Convenience extension converting comma separated list of strings into lifetime list
 */
val String.asLifetimes get() = this.split(",").map { lifetime(it) }

fun lifetimes(vararg lifetime: String) = lifetime.map {
    lifetime(it)
}

/** Convenience extension transforming list of lifetimes to its rust text */
val List<Lifetime>.asRust
    get() = if (this.isNotEmpty()) {
        "<${this.joinToString(", ") { it.asRust }}>"
    } else {
        ""
    }

val static = Lifetime("static")
