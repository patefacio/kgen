package kgen.rust.clap_binary

import kgen.*

data class ClapCommand(
    val nameId: String,
    val clapArgs: List<ClapArg> = emptyList()
    ) : Identifier(nameId) {

}
