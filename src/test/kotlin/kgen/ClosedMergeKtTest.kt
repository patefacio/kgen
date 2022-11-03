package kgen

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File
import kgen.markdownDelimiter

internal class ClosedMergeKtTest {

    val blockName1 = "block_name<foo>"
    val blockName2 = "block_name<bar>"
    val blockName3 = "foobar"

    fun wrapOpen(n: String) = "<!--- α <$n> -->"
    fun wrapClose(n: String) = "<!--- ω <$n> -->"

    val block1 = """
        ${wrapOpen(blockName1)}
        hand written text to preserve
        ${wrapClose(blockName1)}""".trimIndent()

    val block2 = """
        ${wrapOpen(blockName2)}
        more hand written text to preserve
        ${wrapClose(blockName2)}""".trimIndent()

    val block3 = """
        <!--- α `$blockName3` -->
        hand written with different naming
        <!--- ω `$blockName3` -->""".trimIndent()

    val defunctBlock = markdownDelimiter.emptyBlock("defunct")

    val emptyGenerated = """
generated prefix text
${markdownDelimiter.emptyBlock(blockName1)}
preserved
${markdownDelimiter.emptyBlock(blockName2)}
also preserved
${markdownDelimiter.emptyBlock(blockName3, blockNameDelimiter = BlockNameDelimiter.BackTick)}
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
${markdownDelimiter.emptyBlock(blockName1)}
preserved *updated2*
${markdownDelimiter.emptyBlock(blockName2)}
also preserved *updated3*
${markdownDelimiter.emptyBlock(blockName3, blockNameDelimiter = BlockNameDelimiter.BackTick)}
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
        val results = markdownDelimiter.pullBlocks(sampleText)

        assertEquals(
            mapOf(
                "block_name<foo>" to block1,
                "block_name<bar>" to block2,
                "foobar" to block3,
                "defunct" to defunctBlock
            ),
            results
        )
    }

    @Test
    fun mergeBlocksTest() {
        val merged = mergeBlocks(generated = newGeneratedText, prior = sampleText, markdownDelimiter)
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
            mergeGeneratedWithFile(emptyGenerated, targetFilePath, markdownDelimiter, announceUpdates = false)
        )

        assertEquals(
            MergeResult(targetFilePath, emptyGenerated, MergeFileStatus.NoChange),
            mergeGeneratedWithFile(emptyGenerated, targetFilePath, markdownDelimiter, announceUpdates = false)
        )

        // Write the sampleText including handwritten text to test merge
        targetFile.writeText(sampleText)

        assertEquals(
            MergeResult(targetFilePath, expectedAfterMerge, MergeFileStatus.Updated),
            mergeGeneratedWithFile(newGeneratedText, targetFilePath, markdownDelimiter, announceUpdates = false)
        )

        targetFile.delete()
    }

    @Test
    fun mergeWithProvidedEmptyContent() {
        val nonEmptyBlock = markdownDelimiter.emptyBlock("not_really_empty", emptyContents = "TODO!()\n")
        val prior = """
Foo
<!--- α <not_really_empty> -->
hand-coded stuff here!!
<!--- ω <not_really_empty> -->
""".trimIndent()

        assertEquals(
            prior,
            mergeBlocks(
                generated = """
Foo
$nonEmptyBlock
""".trimIndent(),
                prior = prior,
                markdownDelimiter
            )
        )

        val newPrior = """
FooBar
<!--- α <not_really_empty> -->
hand-coded stuff here!!
<!--- ω <not_really_empty> -->
""".trimIndent()

        assertEquals(
            newPrior,
            mergeBlocks(newPrior, prior, markdownDelimiter)
        )

        assertEquals(
            nonEmptyBlock,
            mergeBlocks(nonEmptyBlock, nonEmptyBlock, markdownDelimiter)
        )
    }
}