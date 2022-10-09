package kgen

import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

data class BlockDelimiter(val open: String, val close: String)

val alphaOmegaDelimiter = BlockDelimiter(open = "// α", close = "// ω")
val scriptDelimiter = BlockDelimiter(open = "# α", close = "# ω")

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
}

fun emptyBlock(
    blockName: String,
    blockDelimiter: BlockDelimiter = alphaOmegaDelimiter,
    blockNameDelimiter: BlockNameDelimiter = BlockNameDelimiter.AngleBracket
) = """${blockDelimiter.open} ${blockNameDelimiter.open}$blockName${blockNameDelimiter.close}
${blockDelimiter.close} ${blockNameDelimiter.open}$blockName${blockNameDelimiter.close}
""".trimIndent()

fun pullBlocks(text: String, blockDelimiter: BlockDelimiter = alphaOmegaDelimiter): Map<String, String> {
    val open = blockDelimiter.open
    val close = blockDelimiter.close

    // The block name is within angle brackets or back ticks and the name is *captured/grouped*
    val blockName = """([<`]([^\n]*)[>`])[ \t]*"""
    val block = """.*?"""

    // The regex ties the named block in open tag to close tag
    val splitterRe = """$open\s+$blockName$block$close\s+\1"""
        .toRegex(options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))

    return splitterRe.findAll(text).associate {
        Pair(it.groupValues[2], it.value)
    }
}

fun mergeBlocks(generated: String, prior: String, blockDelimiter: BlockDelimiter = alphaOmegaDelimiter): String {
    val generatedBlocks = pullBlocks(generated, blockDelimiter)
    val priorBlocks = pullBlocks(prior, blockDelimiter)
    var result = generated

    priorBlocks.entries.forEach { (blockName, block) ->
        val generatedBlock = generatedBlocks.get(blockName)
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
            if(!parentPath.exists() && !parentPath.toFile().mkdirs()) {
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