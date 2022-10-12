package kgen.utility

/** Set of common regular expressions.
 *
 */
enum class CommonRegexes {
    GrepRustComment,
    GrepFileLineNumberAndMatch;

    companion object {
        val grepRustCommentRegex = """^[^:]+:\d+:\s*//""".toRegex()
        val grepFileLineNumberAndMatch = """^([^:]+):(\d+):(.*)""".toRegex()
    }

    fun asRegex() = when (this) {
        GrepRustComment -> grepRustCommentRegex
        GrepFileLineNumberAndMatch -> grepFileLineNumberAndMatch
    }
}