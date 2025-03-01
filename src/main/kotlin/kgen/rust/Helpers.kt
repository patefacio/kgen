package kgen.rust

import kgen.indent
import kgen.joinNonEmpty

fun mutable(isMutable: Boolean) = if (isMutable) {
    "mut"
} else {
    ""
}

/** Text to announce a section of rust items.
 *
 */
fun announceSection(label: String, content: String) = when (content) {
    "" -> ""
    else -> """
////////////////////////////////////////////////////////////////////////////////////
// --- $label ---
////////////////////////////////////////////////////////////////////////////////////
$content""".trimIndent()
}

/** Convert content into an announcement section with a label */
fun announceSection(label: String, content: List<String>): String =
    announceSection(label, content.joinNonEmpty())

/** Transform a string of [text] that is wrapped with `//!` prefixes for each line */
fun innerDoc(text: String?) = text
    ?.split("\n")
    ?.joinToString("\n") { "//! $it" }

/** Transform the [text], like a function signature, into the [text] followed by
 * its [genericParamSet] converted into a where clause.
 *
 * @property text The text, for example the signature of a function without its bindings
 * @property genericParamSet The generic praraters comprising the where clasuse with its bindings.
 */
fun withWhereClause(text: String, genericParamSet: GenericParamSet?): String {
    val whereClause = genericParamSet?.whereClause
    return if (whereClause == null) {
        text
    } else {
        "$text\nwhere\n${indent(whereClause)}"
    }
}