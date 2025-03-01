package kgen.rust.clap_binary

import kgen.Identifier
import kgen.missingDoc

data class ClapCommand(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Command"),
    val clapArgs: List<ClapArg> = emptyList()
) : Identifier(nameId)
