package kgen.rust

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

fun missingDoc(itemName: String, itemType: String) = "TODO: Document $itemType($itemName)"