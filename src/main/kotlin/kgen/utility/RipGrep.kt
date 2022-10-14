package kgen.utility

import java.nio.file.Path

/** Wraps common ripgrep usage into basic object
 * @property expression Expression to search
 * @property paths List of paths to search
 * @property fileExclusions File globs to be excluded (converted to `-g!_EXPRESSION_`)
 * @property filesOnly Sets flag to not show match, just the file names with matches
 * @property excludeLineRegexes Post run filters out based on these regexes
 * @property commonExclusions Post run filters out based on these regexes
 * @property wordBoundaryOnly Sets `-w` flag that treats expression as having word boundary
 */
data class RipGrep(
    val expression: String,
    val paths: List<Path>,
    val fileExclusions: List<String> = emptyList(),
    val filesOnly: Boolean = false,
    val excludeLineRegexes: List<Regex> = emptyList(),
    val commonExclusions: List<CommonRegexes> = emptyList(),
    val wordBoundaryOnly: Boolean = false,
    val usePcre2: Boolean = false
) {

    constructor(
        expression: String,
        vararg paths: Path,
        fileExclusions: List<String> = emptyList(),
        filesOnly: Boolean = false,
        excludeLineRegexes: List<Regex> = emptyList(),
        commonExclusions: List<CommonRegexes> = emptyList(),
        wordBoundaryOnly: Boolean = false,
        usePcre2: Boolean = false
    ) : this(
        expression, paths.map { it }, fileExclusions = fileExclusions,
        filesOnly = filesOnly,
        excludeLineRegexes,
        commonExclusions,
        wordBoundaryOnly,
        usePcre2
    )

    val asCommand
        get() = listOf(
            listOfNotNull(
                "rg --no-heading",
                if (filesOnly) {
                    "-l"
                } else {
                    null
                },
                if (wordBoundaryOnly) {
                    "-w"
                } else {
                    ""
                },
                if (usePcre2) {
                    "--pcre2"
                } else {
                    ""
                }

            ),
            fileExclusions.map { "-g!$it" },
            listOf("-n -e'$expression'"),
            paths
        ).flatten().joinToString(" ")

    /** Run the search and return the matching hits as list of lines */
    fun search(): List<String> {
        val exclusions = excludeLineRegexes + commonExclusions.map { it.asRegex() }
        val outputLines = asCommand
            // error code 1 implies no matches, not really an error
            .runShellCommand(ignoreErrors = setOf(1))!!
            .split("\n")

        val lines = outputLines.take(outputLines.size - 1)

        return if (exclusions.isEmpty()) {
            lines
        } else {
            lines.filter { line -> exclusions.all { !it.containsMatchIn(line) } }
        }
    }

}