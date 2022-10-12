package kgen

import java.nio.file.Paths

fun trailingText(text: CharSequence?, trailing: String = " ") = if (text.isNullOrEmpty()) {
    text
} else {
    "$text$trailing"
}

fun leadingText(text: String, leading: String = " ") = if (text.isEmpty()) {
    ""
} else {
    "$leading$text"
}

fun tripleQuote(text: String) = "\"\"\"$text\"\"\""

fun doubleQuote(text: String) = "\"$text\""

fun singleQuote(text: String) = "'$text'"

fun backtickQuote(text: String) = "`$text`"

fun List<String>.joinNonEmpty(separator: CharSequence = "\n") = this.filter { it.isNotEmpty() }.joinToString(separator)

fun joinNonEmpty(vararg text: String, separator: CharSequence = "\n") = text.toList().joinNonEmpty(separator)

fun indent(text: CharSequence?, indent: String = "  ") = text
    ?.split("\n")
    ?.joinToString("\n") { "$indent$it" }

fun bracketText(text: String, open: String = "{", close: String = "}") =
    if (text.isEmpty()) {
        "$open$close"
    } else {
        listOf(open, text, close).joinToString("\n")
    }

val whiteSpaceRe = """^\s+$""".toRegex()
fun emptyIfOnlyWhitespace(text: String) =
    text.replace(whiteSpaceRe, "")

val String.nullIfEmpty
    get() = if (this.isNullOrEmpty()) {
        null
    } else {
        this
    }

val CharSequence?.emptyIfNull
    get() = this ?: ""


val String.asPath get() = Paths.get(this)