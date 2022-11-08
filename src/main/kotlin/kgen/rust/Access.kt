package kgen.rust

sealed class Access : AsRust {
    object ReadOnly : Access()
    object ReadOnlyRef : Access()
    object ReadWrite : Access()
    object ReadWriteRef : Access()
    object Inaccessible : Access()

    object Pub : Access()
    object PubCrate : Access()
    object PubSelf : Access()
    object PubSuper : Access()
    class PubIn(val inPackage: String): Access()
    object None : Access()

    override val asRust get() = when(this) {
        None, ReadOnly, ReadOnlyRef, ReadWrite, ReadWriteRef, Inaccessible -> ""
        Pub -> "pub"
        PubCrate -> "pub(crate)"
        PubSelf -> "pub(self)"
        PubSuper -> "pub(super)"
        is PubIn -> "pub(in $inPackage)"
        else -> throw RuntimeException("$this can not be summarized `asCode`")
    }

    val requiresReader get() = when(this){
        ReadOnly, ReadWrite -> true
        else -> false
    }

    val requiresRefReader get() = when(this) {
        ReadOnlyRef, ReadWriteRef -> true
        else -> false
    }

    val requiresWriter get() = when(this) {
        ReadWrite -> true
        else -> false
    }

    val requiresRefWriter get() = when(this) {
        ReadWriteRef -> true
        else -> false
    }


}
