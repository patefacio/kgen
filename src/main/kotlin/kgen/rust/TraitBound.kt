package kgen.rust

sealed class TraitBound : AsRust {
    class Trait(val trait: Trait) : TraitBound() {
        override val asRust: String
            get() = trait.asRust
    }

    class Unmodeled(val traitName: String) : TraitBound() {
        override val asRust: String
            get() = traitName
    }
}