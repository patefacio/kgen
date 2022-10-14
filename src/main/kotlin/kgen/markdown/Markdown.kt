package kgen

import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.math.max

val List<String>.asMarkdownList get() = this.joinToString("\n") { "- $it" }

val List<String>.asMarkdownBlockQuote get() = this.joinToString("\n") { it.prependIndent("> ") }
val String.asMarkdownBlockQuote get() = this.split("\n").asMarkdownBlockQuote

fun List<List<String>>.asMarkdownTable(
    addBlankHeader: Boolean = false,
    header: List<String> = emptyList()
) = if (this.isNotEmpty()) {
    val data = when {
        header.isNotEmpty() -> listOf(header) + this
        addBlankHeader -> listOf(this.first().map { "" }) + this
        else -> this
    }.withNormalizedWidths

    val divider = data.first().map { "".padEnd(it.length, '-') }
    (listOf(
        data.first(),
        divider
    ) + data.drop(1))
        .map { cells ->
            cells.withIndex().map { (i, cell) ->
                when (i) {
                    0 -> "|$cell"
                    cells.lastIndex -> "$cell|"
                    else -> cell
                }
            }.joinToString("|")
        }
        .joinToString("\n")
} else {
    ""
}


val List<List<String>>.widths
    get() = if (this.isEmpty()) {
        emptyList()
    } else {
        val widths = MutableList(this.first().size) { 0 }

        this.forEach { cells ->
            if (widths.size < cells.size) {
                repeat(cells.size - widths.size) { widths.add(0) }
            }
            cells.withIndex().forEach { (j, cell) ->
                widths[j] = max(widths[j], cell.length)
            }
        }
        widths
    }

val List<List<String>>.withNormalizedWidths
    get() = if (this.isNotEmpty()) {
        val widths = this.widths

        this.map { cells ->
            widths
                .withIndex()
                .map { (j, width) -> cells.getOrElse(j) { "" }.padEnd(width) }
        }
    } else {
        this
    }

data class Markdown(
    val title: String? = null,
    val subtitle: String? = null,
    val body: String? = null
) {

    private val markdownTitle
        get() = if (title != null) {
            "# $title"
        } else {
            null
        }

    private val markdownSubtitle
        get() = if (subtitle != null) {
            "## $subtitle"
        } else {
            null
        }

    val asMarkdown get() = listOfNotNull(markdownTitle, markdownSubtitle, body).joinToString("\n\n")

    fun checkWriteFile(path: Path) = kgen.checkWriteFile(path.pathString, asMarkdown)
}


fun String.asLanguageBlock(language: String) = "```$language\n$this\n```"
val String.asRustBlock get() = this.asLanguageBlock("rust")
val String.asKotlinBlock get() = this.asLanguageBlock("kotlin")
val String.emphasize
    get() = if (this.isEmpty()) {
        ""
    } else {
        "`$this`"
    }

val String.bold
    get() = if (this.isEmpty()) {
        ""
    } else {
        "**$this**"
    }