package kgen.rust

import kgen.Identifier
import kgen.missingDoc

data class Binary(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Binary"),
    val module: Module
) : Identifier(nameId) {
}