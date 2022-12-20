package kgen

import java.io.File
import kotlin.io.path.exists
import kotlin.io.path.pathString

/**
 * A **Block** is a section of text that is intended to be protected during the code
 * generation process. The [BlockDelimiter] provides a way to signify where the
 * protected text blocks are among code in the file that is intended to be generated.
 *
 * The common type of protected block has a simple [OpenBlockDelimiter], for example:
 *
 * ```rust
 *     fn index_mapping(&self) -> HashMap<SystemGrowthId, usize> {
 *          // α <fn index_mapping>
 *
 *          self.identifiers
 *          .iter()
 *          .enumerate()
 *          .map(|(i, id)| (*id, i))
 *          .into_iter()
 *          .collect()
 *
 *          // ω <fn index_mapping>
 *    }
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
 * It is a called `ClosedBlock` simply because the delimiter is a [ClosedBlockDelimiter]
 * since it has an open `<!--- [α]` and a close `-->` **within the delimiter itself**.
 */
interface BlockDelimiter {
    val open: String
    val close: String

    fun pullBlocks(text: String): Map<String, String>

    /** Create an empty block.
     * @param name Name used in protection block delimiter
     * @param blockNameDelimiter Either single back-tick (e.g. if name has templates) or angle brackets
     * @param emptyContents An empty block is usually just the delimiters and a new-line, but sometimes
     *                      it is useful to create an empty block with something real like a `todo!()`
     *                      that will be used to track progress (e.g. in a test function)
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
    override fun pullBlocks(text: String): Map<String, String> {

        // The block name is within angle brackets or back ticks and the name is *captured/grouped*
        val blockNameMatch = """([<`]([^\n]*)[>`])[ \t]*"""
        val block = """.*?"""

        // The regex ties the named block in open tag to close tag
        val splitterRe = """$open\s+$blockNameMatch$block$close\s+\1"""
            .toRegex(options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))

        return splitterRe.findAll(text).associate {
            Pair(it.groupValues[2], it.value)
        }
    }

    override fun emptyBlock(
        name: String, blockNameDelimiter: BlockNameDelimiter, emptyContents: String?
    ) = """${open} ${blockNameDelimiter.open}$name${blockNameDelimiter.close}
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
    override fun pullBlocks(text: String): Map<String, String> {
        // The block name is within angle brackets or back ticks and the name is *captured/grouped*
        val escapeChars = """([<>!])""".toRegex()
        val blockNameMatch = """([<`]([^\n]*)[>`])[ \t]*"""
        val escapedOpen = open.replace(escapeChars, """\\$1""")
        val escapedClose = close.replace(escapeChars, """\\$1""")
        val wrappedOpen = escapedOpen.replace(blockNamePlaceholder, blockNameMatch)
        val wrappedClose = escapedClose.replace(blockNamePlaceholder, blockNameMatch)
        val block = """.*?"""

        // The regex ties the named block in open tag to close tag
        val splitterRe = """$wrappedOpen\s+$block$wrappedClose"""
            .toRegex(options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))

        return splitterRe.findAll(text).associate {
            Pair(it.groupValues[2], it.value)
        }
    }

    override fun emptyBlock(
        name: String,
        blockNameDelimiter: BlockNameDelimiter,
        emptyContents: String?): String {

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
            result = result.replace(generatedBlock, block)
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
                    file.writeText(content)
                    MergeFileStatus.Updated
                }
            }
        }

        else -> {
            val parentPath = file.toPath().parent
            if (!parentPath.exists() && !parentPath.toFile().mkdirs()) {
                throw RuntimeException("Unable to create folder ${parentPath.pathString}")
            }
            file.writeText(content)
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