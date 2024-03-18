package kgen.utility

import kgen.Block
import kgen.BlockDelimiter
import kgen.alphaOmegaDelimiter

sealed class TextItem {
    data class Text(val text: String) : TextItem()
    data class ProtectBlock(val block: Block) : TextItem()
}

val String.asTextItem get() = TextItem.Text(this)
val Block.asTextItem get() = TextItem.ProtectBlock(this)

typealias TextList = List<TextItem>

val TextList.protectBlockMap get() = this
    .filterIsInstance<TextItem.ProtectBlock>().associateBy { protectBlock -> protectBlock.block.name }

fun TextList.mergePriorInto(
    prior: String,
    blockDelimiter: BlockDelimiter = alphaOmegaDelimiter
): String {
    val priorBlocks = blockDelimiter.pullBlocks(prior)
    val mergedBlocks = protectBlockMap.mapValues {(blockName, protectBlock) ->
        protectBlock.block.copy(body = priorBlocks[blockName]?.body)
    }
    return this.joinToString("\n") { textItem ->
        if (textItem is TextItem.Text) {
            textItem.text
        } else {
            val blockName = (textItem as TextItem.ProtectBlock).block.name
            mergedBlocks[blockName].toString()
        }
    }
}