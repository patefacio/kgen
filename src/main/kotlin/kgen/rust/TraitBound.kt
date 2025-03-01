package kgen.rust

/**
 * Represents a trait bound in Rust, which may include a specified default implementation.
 *
 * This sealed class provides two concrete implementations:
 * - [Trait]: Represents a modeled trait bound with a known trait.
 * - [Unmodeled]: Represents a trait bound with an unmodeled or unknown trait name.
 *
 * @property default An optional default implementation value for the trait bound. Defaults to `null` if not specified.
 */
sealed class TraitBound(val default: String? = null) : AsRust {

    /**
     * Represents a modeled trait bound for a known trait.
     *
     * @property trait The known trait associated with this trait bound.
     * @property default An optional default implementation value for the trait bound. Defaults to `null`.
     */
    class Trait(val trait: Trait, default: String? = null) : TraitBound(default) {

        /**
         * Generates the Rust representation of the trait bound, including the default if specified.
         *
         * Example:
         * - Without default: `TraitName`
         * - With default: `TraitName = DefaultValue`
         */
        override val asRust: String
            get() = withDefault(trait.asRust, default)
    }

    /**
     * Represents an unmodeled or unknown trait bound, identified by a trait name.
     *
     * @property traitName The name of the unmodeled trait.
     * @property default An optional default implementation value for the trait bound. Defaults to `null`.
     */
    class Unmodeled(private val traitName: String, default: String? = null) : TraitBound(default) {

        /**
         * Generates the Rust representation of the unmodeled trait bound, including the default if specified.
         *
         * Example:
         * - Without default: `UnmodeledTraitName`
         * - With default: `UnmodeledTraitName = DefaultValue`
         */
        override val asRust: String
            get() = withDefault(traitName, default)
    }

    /**
     * Appends a default value to the Rust representation of a trait bound, if a default is specified.
     *
     * @param text The Rust representation of the trait bound.
     * @param default The optional default value to append.
     * @return The Rust representation with the default value, or just the trait bound if no default is specified.
     *
     * Example:
     * - Input: `text = "TraitName"`, `default = "DefaultValue"`
     * - Output: `TraitName = DefaultValue`
     */
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
val List<String>.asTraitBoundList get() = this.map { it.asTraitBound }