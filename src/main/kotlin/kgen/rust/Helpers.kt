package kgen.rust

import kgen.joinNonEmpty

fun mutable(isMutable: Boolean) = if (isMutable) {
    "mut"
} else {
    ""
}

fun announceSection(label: String, content: String, separator: String = "\n\n") = when (content) {
    "" -> ""
    else -> """
////////////////////////////////////////////////////////////////////////////////////
// --- $label ---
////////////////////////////////////////////////////////////////////////////////////
$content""".trimIndent()
}

fun announceSection(label: String, content: List<String>, separator: String = "\n\n"): String =
    announceSection(label, content.joinNonEmpty(), separator)