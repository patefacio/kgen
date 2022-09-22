package kgen

import java.io.File

data class BlockDelimiter(val open: String, val close: String)

val alphaOmegaDelimiter = BlockDelimiter(open = "// α", close = "// ω")

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
) = """
${blockDelimiter.open} ${blockNameDelimiter.open}$blockName${blockNameDelimiter.close}
${blockDelimiter.close} ${blockNameDelimiter.open}$blockName${blockNameDelimiter.close}
""".trimIndent()

fun pullBlocks(text: String, blockDelimiter: BlockDelimiter = alphaOmegaDelimiter): Map<String, String> {
    val open = blockDelimiter.open
    val close = blockDelimiter.close

    // The block name is within angle brackets or back ticks and the name is *captured/grouped*
    val blockName = """[<`]([^\n]*)[>`][ \t]*"""
    val block = """(?:.|\n)*?"""

    // The regex ties the named block in open tag to close tag
    val splitterRe = """$open\s+$blockName$block$close\s+[<`]\1[>`]""".toRegex(option = RegexOption.MULTILINE)

    return splitterRe.findAll(text).associate {
        Pair(it.groupValues[1], it.value)
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

fun checkWriteFile(
    filePath: String,
    content: String,
    previousContent: String? = null,
    announceUpdates: Boolean = true
): MergeFileStatus {
    val file = File(filePath)
    val mergeFileStatus = when {
        file.exists() -> {
            val previous = previousContent ?: file.readText()
            when {
                previous == content -> MergeFileStatus.NoChange
                else -> {
                    file.writeText(content)
                    MergeFileStatus.Updated
                }
            }
        }

        else -> {
            file.writeText(content)
            MergeFileStatus.Created
        }
    }

    if(announceUpdates) {
        println(mergeFileStatus.announce(filePath))
    }

    return mergeFileStatus
}

fun mergeGeneratedWithFile(
    generated: String,
    filePath: String,
    blockDelimiter: BlockDelimiter = alphaOmegaDelimiter,
    announceUpdates: Boolean = true
): Pair<MergeFileStatus, String> {
    val file = File(filePath)

    return if (file.exists()) {
        val fileContent = file.readText()
        val mergedContent = mergeBlocks(generated, fileContent, blockDelimiter)
        Pair(checkWriteFile(filePath, mergedContent, fileContent, announceUpdates), mergedContent)
    } else {
        Pair(checkWriteFile(filePath, generated, announceUpdates = announceUpdates), generated)
    }
}