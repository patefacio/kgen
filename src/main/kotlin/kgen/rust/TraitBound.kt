package kgen.rust

sealed class TraitBound(val default: String? = null) : AsRust {
    class Trait(val trait: Trait, default: String? = null) : TraitBound(default) {
        override val asRust: String
            get() = withDefault(trait.asRust, default)
    }

    class Unmodeled(private val traitName: String, default: String? = null) : TraitBound(default) {
        override val asRust: String
            get() = withDefault(traitName, default)
    }

    fun withDefault(text: String, default: String?) = if (default == null) {
        text
    } else {
        "$text = $default"
    }
}

/** Convert string to [TraitBound] */
val String.asTraitBound get() = TraitBound.Unmodeled(this)

/** Convert string to list of single [TraitBound] */
val String.asTraitBoundList get() = listOf(this.asTraitBound)

/** Convert list of strings to list of [TraitBound] */
val List<String>.asTraitBoundList get() = this.map { it.asTraitBound}