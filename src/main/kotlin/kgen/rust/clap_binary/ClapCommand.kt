package kgen.rust.clap_binary

import kgen.*

data class ClapCommand(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Command"),
    val clapArgs: List<ClapArg> = emptyList()
    ) : Identifier(nameId) {

}
