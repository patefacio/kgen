package kgen

import java.nio.file.Paths

/** Append `trailing` string to `text` if `text` has content.
 * If text is null or empty the trailer is not added.
 * @param text Text to add trailing text to
 * @param trailing _Trailing_ text to add
 */
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

fun charQuote(c: Char) = "'$c'"

fun singleQuote(text: String) = "'$text'"

fun backtickQuote(text: String) = "`$text`"

fun rustQuote(text: String) = "r#\"$text\"#"

fun List<String>.joinNonEmpty(separator: CharSequence = "\n") = this.filter { it.isNotEmpty() }.joinToString(separator)

fun joinNonEmpty(vararg text: String, separator: CharSequence = "\n") = text.toList().joinNonEmpty(separator)

fun indent(text: CharSequence?, indent: String = "  ") = text
    ?.split("\n")
    ?.joinToString("\n") { "$indent$it" }

fun String?.cgIndent(indent: String = "  "): String = indent(this, indent)!!
val String?.indented get() = this?.cgIndent()

fun bracketText(text: String, open: String = "{", close: String = "}") =
    if (text.isEmpty()) {
        "$open$close"
    } else {
        listOf(open, text, close).joinToString("\n")
    }

val whiteSpaceRe = """^\s+$""".toRegex()
fun emptyIfOnlyWhitespace(text: String) =
    text.replace(whiteSpaceRe, "")

private val unboundedWhiteSpace = """\s+""".toRegex()

/** Removes whitespace from string */
val String.noWhitespace get() = this.replace(unboundedWhiteSpace, "")

val String.nullIfEmpty
    get() = this.ifEmpty {
        null
    }

val CharSequence?.emptyIfNull
    get() = this ?: ""


val String.asPath get() = Paths.get(this)

fun wordWrap(text: String, lineWidth: Int = 80): String {
    val words = text.split(' ')
    val sb = StringBuilder(words[0])
    var spaceLeft = lineWidth - words[0].length
    for (word in words.drop(1)) {
        val len = word.length
        if (len + 1 > spaceLeft) {
            sb.append("\n").append(word)
            spaceLeft = lineWidth - len
        } else {
            sb.append(" ").append(word)
            spaceLeft -= (len + 1)
        }
    }
    return sb.toString()
}

fun wrapIndent(text: String, lineWidth: Int = 76, indent: String = "    ") =
    wordWrap(text, lineWidth).replaceIndent(indent)

fun wrapIndentExceptFirst(text: String, lineWidth: Int = 76, indent: String = "    ") =
    wrapIndent(text, lineWidth, indent).replaceFirst(indent, "")

fun wrapParamDoc(paramPrefixLength: Int, doc: String): String {
    val indent = " ".repeat(paramPrefixLength)
    return wrapIndentExceptFirst(doc, 80 - paramPrefixLength, indent)
}