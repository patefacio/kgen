package kgen.rust

sealed class TraitBound(val default: String? = null) : AsRust {
    class Trait(val trait: Trait, default: String? = null) : TraitBound(default) {
        override val asRust: String
            get() = withDefault(trait.asRust, default)
    }

    class Unmodeled(val traitName: String, default: String? = null) : TraitBound(default) {
        override val asRust: String
            get() = withDefault(traitName, default)
    }

    fun withDefault(text: String, default: String?) = if (default == null) {
        text
    } else {
        "$text = $default"
    }
}