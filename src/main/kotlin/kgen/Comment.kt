package kgen

const val doubleOpener = "// "
const val tripleOpener = "/// "
const val scriptOpener = "# "

private val verticalTrimRightRe = """[\r|\n]+$""".toRegex()

fun comment(text: String, opener: String = doubleOpener) = opener + text
    .replace(verticalTrimRightRe, "")
    .split("\n")
    .joinToString("\n$opener")

fun commentTriple(text: String) = if (text.isEmpty()) {
    text
} else {
    comment(text, tripleOpener)
}

fun commentScript(text: String) = comment(text, scriptOpener)

fun blockComment(text: String, indent: String = "  ", separator: String = "\n") = listOf(
    "/*", comment(text, indent), "*/"
).joinToString(separator)


val String?.commentTriple
    get() = if (this != null) {
        commentTriple(this)
    } else {
        null
    }

val String?.comment
    get() = if (this != null) {
        comment(this)
    } else {
        null
    }
