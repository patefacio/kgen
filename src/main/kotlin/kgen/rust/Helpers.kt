package kgen.rust

import kgen.indent
import kgen.joinNonEmpty

fun mutable(isMutable: Boolean) = if (isMutable) {
    "mut"
} else {
    ""
}

fun announceSection(label: String, content: String) = when (content) {
    "" -> ""
    else -> """
////////////////////////////////////////////////////////////////////////////////////
// --- $label ---
////////////////////////////////////////////////////////////////////////////////////
$content""".trimIndent()
}

fun announceSection(label: String, content: List<String>): String =
    announceSection(label, content.joinNonEmpty())

fun innerDoc(text: String?) = text
    ?.split("\n")
    ?.joinToString("\n") { "//! $it" }

fun withWhereClause(text: String, genericParamSet: GenericParamSet?): String {
    val whereClause = genericParamSet?.whereClause
    return if (whereClause == null) {
        text
    } else {
        "$text\nwhere\n${indent(whereClause)}"
    }
}