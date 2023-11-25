package kgen.rust

sealed class Access : AsRust {
    data object ReadOnly : Access()
    data object ReadOnlyCloned : Access()
    data object ReadOnlyRef : Access()
    data object ReadWrite : Access()
    data object ReadWriteRef : Access()
    data object Inaccessible : Access()

    data object Pub : Access()
    data object PubCrate : Access()
    data object PubSelf : Access()
    data object PubSuper : Access()
    class PubIn(val inPackage: String) : Access()
    data object None : Access()

    override val asRust
        get() = when (this) {
            None, ReadOnly, ReadOnlyCloned, ReadOnlyRef, ReadWrite, ReadWriteRef, Inaccessible -> ""
            Pub -> "pub"
            PubCrate -> "pub(crate)"
            PubSelf -> "pub(self)"
            PubSuper -> "pub(super)"
            is PubIn -> "pub(in $inPackage)"
        }

    val requiresReader
        get() = when (this) {
            ReadOnly, ReadWrite -> true
            else -> false
        }

    val requiresReaderCloned
        get() = when (this) {
            ReadOnlyCloned -> true
            else -> false
        }

    val requiresRefReader
        get() = when (this) {
            ReadOnlyRef, ReadWriteRef -> true
            else -> false
        }

    val requiresWriter
        get() = when (this) {
            ReadWrite -> true
            else -> false
        }

    val requiresRefWriter
        get() = when (this) {
            ReadWriteRef -> true
            else -> false
        }


}
