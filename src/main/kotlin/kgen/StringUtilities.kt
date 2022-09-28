package kgen

fun trailingText(text: String, trailing: String = " ") = if (text.isEmpty()) {
    ""
} else {
    "$text$trailing"
}

fun leadingText(text: String, leading: String = " ") = if(text.isEmpty()) {
    ""
} else {
    "$leading$text"
}

fun tripleQuote(text: String) = "\"\"\"$text\"\"\""

fun doubleQuote(text: String) = "\"$text\""

fun List<String>.joinNonEmpty(separator: CharSequence = "\n") = this.filter { it.isNotEmpty() }.joinToString(separator)

fun indent(text: String?, indent: String = "  ") = text
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