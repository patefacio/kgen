package kgen.rust.decl_macro

import kgen.emptyOpenDelimitedBlock

data class Rule(
    val match: List<Matcher>,
    val transcriber: Transcriber,
) {

    fun asRust(macroName: String): String {
        val block = emptyOpenDelimitedBlock("$macroName(${match.joinToString(":") { it.asFragId }})")

        return "(${
            match.joinToString(", ")
            { it.asRust }
        }) => {\n$block\n}"
    }

}

fun rule(
    vararg matchers: Matcher,
    transcriber: Transcriber? = null
) = Rule(matchers.toList(), transcriber ?: Transcriber(""))
