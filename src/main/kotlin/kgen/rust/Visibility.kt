package kgen.rust

sealed class Visibility : AsRust {
    data object Pub : Visibility()
    data object PubCrate : Visibility()
    data object PubSelf : Visibility()
    data object PubSuper : Visibility()
    class PubIn(val inPackage: String) : Visibility()

    data object PubExport : Visibility()

    data object PubCrateExport : Visibility()

    val isExport get() = this is PubExport || this is PubCrateExport

    data object None : Visibility()

    override val asRust
        get() = when (this) {
            Pub, PubExport -> "pub"
            PubCrate, PubCrateExport -> "pub(crate)"
            PubSelf -> "pub(self)"
            PubSuper -> "pub(super)"
            is PubIn -> "pub(in $inPackage)"
            None -> ""
        }

    fun makeUse(item: String) =
        Use(pathName = "self::$item", visibility = this)
}