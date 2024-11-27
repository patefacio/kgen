package kgen

import java.io.File
import kotlin.io.path.exists
import kotlin.io.path.pathString

/**
 * A **Block** of code to be protected across regeneration.
 *
 * @property name A name for the bock, used to tag the block for protection on regeneration.
 * @property opener A string used to signify the opening of a protection block
 * @property body A string to be placed in the block. If this is null the block is intended as
 * a _protect block_. If it has content that means the contents are driven by the
 * generator and no protection is required as a regeneration will reproduce the same.
 * @property closer A string used to signify the closing of a protection block
 * @property preserved Source text representing the contents of the block, typically read from disk and
 * intended to be preserved.
 */
data class Block(
    val name: String,
    val opener: String,
    val body: String?,
    val closer: String,
    val preserved: String? = null
) {

    constructor(
        name: String,
        blockDelimiter: BlockDelimiter,
        body: String? = null,
    ) : this(
        name,
        "${blockDelimiter.open} <$name>",
        body,
        "${blockDelimiter.close} <$name>",
        null
    )

    override fun toString() = "$opener\n$body$closer"

    fun replaceBodyWith(otherBlock: Block) = copy(body = otherBlock.body)
}

/**
 * A **Block** is a section of text that is intended to be protected during the code
 * generation process. The [BlockDelimiter] provides a way to signify where the
 * protected text blocks are among code in the file being generated.
 *
 * The common type of protected block has a simple [OpenBlockDelimiter], for example:
 *
 * ```rust
 *fn index_mapping(&self) -> HashMap<SystemGrowthId, usize> {
 *     // α <fn index_mapping>
 *
 *     self.identifiers
 *     .iter()
 *     .enumerate()
 *     .map(|(i, id)| (*id, i))
 *     .into_iter()
 *     .collect()
 *
 *      // ω <fn index_mapping>
 *}
 * ```
 *
 * In this example the function signature is not protected and will get regenerated.
 * The open delimiter has an [open] which is `// α <fn index_mapping>` and a [close],
 * which is `// ω <fn index_mapping>`. It is called an [OpenBlockDelimiter] because
 * the text itself is open-ended (i.e. has some prefix text `// [αω] `) followed by
 * the name of the block.
 *
 * Another type of **block** is a `ClosedBlock`, delimited with a [ClosedBlockDelimiter].
 * For example, some Markdown text may contain:
 *
 * ```markdown
 * # Title
 * <!--- α <fn main_content> -->
 *   Hand-written, protected code here.
 * <!--- ω <fn main_content> -->
 * ```
 *
 * It is a called `ClosedBlock` because the delimiter requires some closing text
 * (e.g. the trailing markdown/html comment end `-->`) within the delimiter itself.
 */
interface BlockDelimiter {
    val open: String
    val close: String

    fun pullBlocks(text: String): Map<String, Block>

    /** Create an empty block in generated content.
     *
     * An example empty block, with an initial _todo_ as a placeholder until the real implementation
     * is written.
     *
     * ```rust
     * fn foo() {
     *   // α <fn foo>
     *   todo!("Implement foo")
     *   // ω <fn foo>
     * }
     * ```
     *
     * @param name The name of the block
     * @param blockNameDelimiter The delimiter
     * @param emptyContents Any contents to be included when the block is otherwise empty. A
     * use case for empty contents might be a block to protect a handwritten function that
     * requires some content before the actual handwritten code is provided.
     *
     */
    fun emptyBlock(
        name: String,
        blockNameDelimiter: BlockNameDelimiter = BlockNameDelimiter.AngleBracket,
        emptyContents: String? = null
    ): String
}

/**
 * A block delimiter that is open-ended and has an [open] (e.g. `// α`) and a
 * corresponding [close] (e.g. `// ω`)
 */
data class OpenBlockDelimiter(override val open: String, override val close: String) : BlockDelimiter {
    override fun pullBlocks(text: String): Map<String, Block> {
        val regexText = """$open\s+(?<blockLabel>[<`](?<blockName>[^\n]*)[>`])[ \t]*\n(?<body>.*?)$close\s+\1"""

        // The regex ties the named block in open tag to close tag
        val splitterRe = regexText
            .toRegex(options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))

        return splitterRe.findAll(text).associate { matchResult ->
            val blockText = matchResult.value
            val blockName = matchResult.groups["blockName"]!!.value
            val blockLabel = matchResult.groups["blockLabel"]!!.value
            val body = matchResult.groups["body"]!!.value
            val block =
                Block(
                    name = blockName,
                    opener = "$open $blockLabel",
                    body = body,
                    closer = "$close $blockLabel",
                    preserved = blockText
                )
            Pair(blockName, block)
        }
    }

    override fun emptyBlock(
        name: String, blockNameDelimiter: BlockNameDelimiter, emptyContents: String?
    ) = """$open ${blockNameDelimiter.open}$name${blockNameDelimiter.close}
${
        trailingText(
            emptyContents,
            "\n"
        ) ?: ""
    }${close} ${blockNameDelimiter.open}$name${blockNameDelimiter.close}
""".trimIndent()
}

/**
 * A block delimiter that has some closing text after the name.
 * For example, an [open] (e.g. `<!--- α <block_name> -->`) and a
 * corresponding [close] (e.g. `<!--- ω <block_name> -->`).
 */
data class ClosedBlockDelimiter(
    override val open: String,
    override val close: String,
    val blockNamePlaceholder: String = "NAME"
) : BlockDelimiter {
    override fun pullBlocks(text: String): Map<String, Block> {
        // The block name is within angle brackets or back ticks and the name is *captured/grouped*
        val escapeChars = """([<>!])""".toRegex()

        // Escape the special characters in the `open` and `close`
        val escapedOpen = open.replace(escapeChars, """\\$1""")
        val escapedClose = close.replace(escapeChars, """\\$1""")

        // Create regexes for open and close with the escaped special characters
        val blockNameMatch = """(?<delimitedBlockName>[<`](?<blockName>[^\n]*)[>`])[ \t]*"""
        val wrappedOpen = escapedOpen.replace(blockNamePlaceholder, blockNameMatch)
        val wrappedClose = escapedClose.replace(blockNamePlaceholder, """\2""")

        val blockMatcher = """.*"""
        val regexText = """(?<openMatch>$wrappedOpen)\n(?<body>$blockMatcher)(?<closeMatch>$wrappedClose)"""

        // The regex ties the named block in open tag to close tag
        val splitterRe = regexText
            .toRegex(options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))

        return splitterRe.findAll(text).associate { matchResult ->
            val blockText = matchResult.value
            val openMatch = matchResult.groups["openMatch"]!!.value
            val closeMatch = matchResult.groups["closeMatch"]!!.value
            val blockName = matchResult.groups["blockName"]!!.value
            val body = matchResult.groups["body"]!!.value
            val block = Block(
                name = blockName,
                opener = openMatch,
                body = body,
                closer = closeMatch,
                preserved = blockText
            )
            Pair(blockName, block)
        }
    }

    /** Create an empty block in generated content.
     *
     * An example empty block, with an initial _todo_ as a placeholder until the real implementation
     * is written.
     *
     * ```rust
     * fn foo() {
     *   // α <fn foo>
     *   todo!("Implement foo")
     *   // ω <fn foo>
     * }
     * ```
     *
     * @param name The name of the block
     * @param blockNameDelimiter The delimiter
     * @param emptyContents Any contents to be included when the block is otherwise empty. A
     * use case for empty contents might be a block to protect a handwritten function that
     * requires some content before the actual handwritten code is provided.
     *
     */
    override fun emptyBlock(
        name: String,
        blockNameDelimiter: BlockNameDelimiter,
        emptyContents: String?
    ): String {

        val wrappedName = blockNameDelimiter.wrapName(name)
        val openDelim = open.replace(blockNamePlaceholder, wrappedName)
        val closeDelim = close.replace(blockNamePlaceholder, wrappedName)

        return """$openDelim
${
            trailingText(
                emptyContents,
                "\n"
            ) ?: ""
        }$closeDelim
""".trimIndent()
    }
}


val alphaOmegaDelimiter = OpenBlockDelimiter(open = "// α", close = "// ω")
val scriptDelimiter = OpenBlockDelimiter(open = "# α", close = "# ω")
val markdownDelimiter = ClosedBlockDelimiter(
    open = "<!--- α NAME -->",
    close = "<!--- ω NAME -->"
)

enum class BlockNameDelimiter {
    AngleBracket,
    BackTick;

    val open
        get() = when (this) {
            AngleBracket -> "<"
            BackTick -> "`"
        }

    val close
        get() = when (this) {
            AngleBracket -> ">"
            BackTick -> "`"
        }

    fun wrapName(name: String) = "$open$name$close"
}

/** Create an empty block.
 * @param blockName Name used in protection block delimiter
 * @param blockDelimiter Type of delimiter (e.g. rust quote for rust, script for scripts)
 * @param blockNameDelimiter Either single quote (e.g. if name has templates) or angle brackets
 * @param emptyContents An empty block is usually just the delimiters and a new-line, but sometimes
 *                      it is useful to create an empty block with something real like a `todo!()`
 *                      that will be used to track progress (e.g. in a test function)
 */
fun emptyOpenDelimitedBlock(
    blockName: String,
    blockDelimiter: BlockDelimiter = alphaOmegaDelimiter,
    blockNameDelimiter: BlockNameDelimiter = BlockNameDelimiter.AngleBracket,
    emptyContents: String? = null
) = blockDelimiter.emptyBlock(blockName, blockNameDelimiter, emptyContents)

/** Create an empty close delimited block.
 * @param blockName Name used in protection block delimiter
 * @param blockDelimiter Type of delimiter (e.g. rust quote for rust, script for scripts)
 * @param blockNameDelimiter Either single quote (e.g. if name has templates) or angle brackets
 * @param emptyContents An empty block is usually just the delimiters and a new-line, but sometimes
 *                      it is useful to create an empty block with something real like a `todo!()`
 *                      that will be used to track progress (e.g. in a test function)
 */
fun emptyCloseDelimitedBlock(
    blockName: String,
    blockDelimiter: BlockDelimiter = alphaOmegaDelimiter,
    blockNameDelimiter: BlockNameDelimiter = BlockNameDelimiter.AngleBracket,
    emptyContents: String? = null
) = """${blockDelimiter.open} ${blockNameDelimiter.open}$blockName${blockNameDelimiter.close}
${
    trailingText(
        emptyContents,
        "\n"
    ) ?: ""
}${blockDelimiter.close} ${blockNameDelimiter.open}$blockName${blockNameDelimiter.close}
""".trimIndent()

fun mergeBlocks(
    generated: String,
    prior: String,
    blockDelimiter: BlockDelimiter = alphaOmegaDelimiter
): String {
    val generatedBlocks = blockDelimiter.pullBlocks(generated)
    val priorBlocks = blockDelimiter.pullBlocks(prior)
    var result = generated

    priorBlocks.entries.forEach { (blockName, block) ->
        val generatedBlock = generatedBlocks[blockName]
        if (generatedBlock != null) {
            if (block.preserved != null) {
                result = result.replace(
                    generatedBlock.preserved!!,
                    block.toString()
                )
            }
        } else {
            kgenLogger.warn { "Missing generated protect block `$blockName` removed!" }
        }
    }

    return result
}

enum class MergeFileStatus {
    Updated,
    NoChange,
    Created;

    fun announce(filePath: String) = when (this) {
        Updated -> "UPDATED: file `$filePath`"
        NoChange -> "NO CHANGE: file `$filePath`"
        Created -> "CREATED: file `$filePath`"
    }
}

data class MergeResult(
    val targetPath: String,
    val mergedContent: String,
    val mergeFileStatus: MergeFileStatus
)

fun checkWriteFile(
    filePath: String,
    content: String,
    previousContent: String? = null,
    announceUpdates: Boolean = true
): MergeResult {
    val file = File(filePath)
    val mergeFileStatus = when {
        file.exists() -> {
            when (previousContent ?: file.readText()) {
                content -> MergeFileStatus.NoChange
                else -> {
                    file.printWriter().use { it.write(content) }
                    MergeFileStatus.Updated
                }
            }
        }

        else -> {
            val parentPath = file.toPath().parent
            if (!parentPath.exists() && !parentPath.toFile().mkdirs()) {
                throw RuntimeException("Unable to create folder ${parentPath.pathString}")
            }
            file.printWriter().use {
                it.write(content)
            }
            MergeFileStatus.Created
        }
    }

    if (announceUpdates) {
        println(mergeFileStatus.announce(filePath))
    }

    return MergeResult(filePath, content, mergeFileStatus)
}

fun mergeGeneratedWithFile(
    generated: String,
    filePath: String,
    blockDelimiter: BlockDelimiter = alphaOmegaDelimiter,
    announceUpdates: Boolean = true
): MergeResult {
    val file = File(filePath)

    return if (file.exists()) {
        val fileContent = file.readText()
        checkWriteFile(filePath, mergeBlocks(generated, fileContent, blockDelimiter), fileContent, announceUpdates)
    } else {
        checkWriteFile(filePath, generated, announceUpdates = announceUpdates)
    }
}