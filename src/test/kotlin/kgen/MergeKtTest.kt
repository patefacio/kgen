package kgen

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File
import kotlin.test.expect

internal class MergeKtTest {

    val blockName1 = "block_name<foo>"
    val blockName2 = "block_name<bar>"
    val blockName3 = "foobar"

    val block1 = """
        // α <$blockName1>
        hand written text to preserve
        // ω <$blockName1>""".trimIndent()

    val block2 = """
        // α <$blockName2>
        more hand written text to preserve
        // ω <$blockName2>""".trimIndent()

    val block3 = """
        // α `$blockName3`
        hand written with different naming
        // ω `$blockName3`""".trimIndent()

    val defunctBlock = emptyBlock("defunct")

    val emptyGenerated = """
generated prefix text
${emptyBlock(blockName1)}
preserved
${emptyBlock(blockName2)}
also preserved
${emptyBlock(blockName3, blockNameDelimiter = BlockNameDelimiter.BackTick)}
generated postfix text
$defunctBlock
        """

    val sampleText = """
generated prefix text
$block1
preserved
$block2
also preserved
$block3
generated postfix text
$defunctBlock
        """

    val newGeneratedText = """
generated prefix text *updated1*
${emptyBlock(blockName1)}
preserved *updated2*
${emptyBlock(blockName2)}
also preserved *updated3*
${emptyBlock(blockName3, blockNameDelimiter = BlockNameDelimiter.BackTick)}
generated postfix text *updated4*
        """

    val expectedAfterMerge = """
generated prefix text *updated1*
$block1
preserved *updated2*
$block2
also preserved *updated3*
$block3
generated postfix text *updated4*
        """

    @Test
    fun pullBlocks() {
        val results = pullBlocks(sampleText)

        assertEquals(
            mapOf(
                "block_name<foo>" to block1,
                "block_name<bar>" to block2,
                "foobar" to block3,
                "defunct" to defunctBlock
            ),
            results)
    }

    @Test
    fun mergeBlocks() {
        val merged = kgen.mergeBlocks(generated = newGeneratedText, prior = sampleText)
        assertEquals(expectedAfterMerge, merged)
    }

    @Test
    fun mergeGeneratedWithFile() {
        val targetFilePath = kotlin.io.path.createTempFile(prefix = "kgen_merge").toString()

        // Delete newly created temp file which will be created by merge call
        val targetFile = File(targetFilePath)
        targetFile.delete()

        assertEquals(
            Pair(MergeFileStatus.Created, emptyGenerated),
            mergeGeneratedWithFile(emptyGenerated, targetFilePath)
        )

        assertEquals(
            Pair(MergeFileStatus.NoChange, emptyGenerated),
            mergeGeneratedWithFile(emptyGenerated, targetFilePath)
        )

        // Write the sampleText including handwritten text to test merge
        targetFile.writeText(sampleText)

        assertEquals(
            Pair(MergeFileStatus.Updated, expectedAfterMerge),
            mergeGeneratedWithFile(newGeneratedText, targetFilePath)
        )

        targetFile.delete()
    }
}