package kgen.utility

import kgen.Block
import kgen.alphaOmegaDelimiter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TextListTest {

    private val completeBlockText = """
Some front matter
// α <foobar>
  this is a test
// ω <foobar>
"""

    private val textList = listOf(
        """
Some front matter""".asTextItem,
        Block(
            "foobar",
            alphaOmegaDelimiter,
        ).asTextItem
    )

    @Test
    fun mergeBlocksTest() {
        assertEquals(
            completeBlockText.trim(),
            textList.mergePriorInto(completeBlockText).trim()
        )
    }
}
