package kgen

private val wordDividerRe = """[_-]""".toRegex()

private val charCaseTransitionRe = """([^\p{Lu}])(\p{Lu})""".toRegex()

fun words(text: String): List<String> =
    when {
        text.contains(wordDividerRe) -> text.lowercase().split(wordDividerRe)
        charCaseTransitionRe.containsMatchIn(text) -> words(
            charCaseTransitionRe
                .replace(text) {
                    "${it.groupValues[1]}_${it.groupValues[2].lowercase()}"
                })

        else -> listOf(text.lowercase())
    }


val List<String>.asSnake get() = this.joinToString("_")
fun asSnake(name: String) = words(name.replace(" ", "-")).asSnake

val String.asSnake get() = asSnake(this)

fun capCamelWords(words: List<String>) = words.map { it.replaceFirstChar { it.uppercase() } }

fun capCamel(text: String, sep: String = "") = capCamelWords(words(text)).joinToString(sep)

fun camel(text: String) = capCamel(text).replaceFirstChar { it.lowercase() }

private val snakeRe = """^[a-z]+[a-z\d]*(?:_[a-z\d]+)*$""".toRegex()

fun isSnake(text: String) = snakeRe.matchEntire(text) != null

fun emacs(text: String) = words(text).joinToString("-")

fun abbrev(text: String) = words(text).map { it.first() }.joinToString("")


