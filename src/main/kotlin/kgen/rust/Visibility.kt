package kgen.rust

sealed class Visibility : AsRust {
    object Pub : Visibility()
    object PubCrate : Visibility()
    object PubSelf : Visibility()
    object PubSuper : Visibility()
    class PubIn(val inPackage: String): Visibility()
    object None : Visibility()

    override val asRust get() = when(this) {
        Pub -> "pub"
        PubCrate -> "pub(crate)"
        PubSelf -> "pub(self)"
        PubSuper -> "pub(super)"
        is PubIn -> "pub(in $inPackage)"
        None -> ""
    }
}