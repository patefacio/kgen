package kgen

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File

internal class OpenMergeKtTest {

    private val blockName1 = "block_name<foo>"
    private val blockName2 = "block_name<bar>"
    private val blockName3 = "foobar"

    private val block1 = """
        // α <$blockName1>
        hand written text to preserve
        // ω <$blockName1>""".trimIndent()

    private val block2 = """
        // α <$blockName2>
        more hand written text to preserve
        // ω <$blockName2>""".trimIndent()

    private val block3 = """
        // α `$blockName3`
        hand written with different naming
        // ω `$blockName3`""".trimIndent()

    private val defunctBlock = emptyOpenDelimitedBlock("defunct")

    private val emptyGenerated = """
generated prefix text
${emptyOpenDelimitedBlock(blockName1)}
preserved
${emptyOpenDelimitedBlock(blockName2)}
also preserved
${emptyOpenDelimitedBlock(blockName3, blockNameDelimiter = BlockNameDelimiter.BackTick)}
generated postfix text
$defunctBlock
        """

    private val sampleText = """
generated prefix text
$block1
preserved
$block2
also preserved
$block3
generated postfix text
$defunctBlock
        """

    private val newGeneratedText = """
generated prefix text *updated1*
${emptyOpenDelimitedBlock(blockName1)}
preserved *updated2*
${emptyOpenDelimitedBlock(blockName2)}
also preserved *updated3*
${emptyOpenDelimitedBlock(blockName3, blockNameDelimiter = BlockNameDelimiter.BackTick)}
generated postfix text *updated4*
        """

    private val expectedAfterMerge = """
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

        assertEquals(
            block1,
            alphaOmegaDelimiter.pullBlocks(block1).values.first().toString()
        )

        val results = alphaOmegaDelimiter.pullBlocks(sampleText)

        assertEquals(
            mapOf(
                "block_name<foo>" to block1,
                "block_name<bar>" to block2,
                "foobar" to block3,
                "defunct" to defunctBlock
            ),
            results.mapValues { it.value.toString() }
        )
    }

    @Test
    fun mergeBlocksTest() {
        val merged = mergeBlocks(generated = newGeneratedText, prior = sampleText)
        assertEquals(expectedAfterMerge, merged)
    }

    @Test
    fun mergeGeneratedWithFile() {
        val targetFilePath = kotlin.io.path.createTempFile(prefix = "kgen_merge").toString()

        // Delete newly created temp file which will be created by merge call
        val targetFile = File(targetFilePath)
        targetFile.delete()

        assertEquals(
            MergeResult(targetFilePath, emptyGenerated, MergeFileStatus.Created),
            mergeGeneratedWithFile(emptyGenerated, targetFilePath, announceUpdates = false)
        )

        assertEquals(
            MergeResult(targetFilePath, emptyGenerated, MergeFileStatus.NoChange),
            mergeGeneratedWithFile(emptyGenerated, targetFilePath, announceUpdates = false)
        )

        // Write the sampleText including handwritten text to test merge
        targetFile.writeText(sampleText)

        assertEquals(
            MergeResult(targetFilePath, expectedAfterMerge, MergeFileStatus.Updated),
            mergeGeneratedWithFile(newGeneratedText, targetFilePath, announceUpdates = false)
        )

        targetFile.delete()
    }

    @Test
    fun mergeWithProvidedEmptyContent() {
        val nonEmptyBlock = emptyOpenDelimitedBlock("not_really_empty", emptyContents = "TODO!()\n")
        val prior = """
Foo
// α <not_really_empty>
hand-coded stuff here!!
// ω <not_really_empty>
""".trimIndent()

        assertEquals(
            prior,
            mergeBlocks(
                generated = """
Foo
$nonEmptyBlock
""".trimIndent(),
                prior = prior
            )
        )

        val newPrior = """
FooBar
// α <not_really_empty>
hand-coded stuff here!!
// ω <not_really_empty>
""".trimIndent()

        assertEquals(
            newPrior,
            mergeBlocks(newPrior, prior)
        )

        assertEquals(
            nonEmptyBlock,
            mergeBlocks(nonEmptyBlock, nonEmptyBlock)
        )
    }
}