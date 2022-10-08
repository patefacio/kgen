package kgen

val doubleOpener = "// "
val tripleOpener = "/// "
val scriptOpener = "# "

private val verticalTrimRightRe = """[\r|\n]+$""".toRegex()

fun comment(text: String, opener: String = doubleOpener) = opener + text
    .replace(verticalTrimRightRe, "")
    .split("\n")
    .joinToString("\n$opener")

fun commentTriple(text: String) = comment(text, tripleOpener)
fun commentScript(text: String) = comment(text, scriptOpener)

fun blockComment(text: String, indent: String = "  ") = listOf(
    "/*", comment(text, indent), "*/"
).joinToString("\n")