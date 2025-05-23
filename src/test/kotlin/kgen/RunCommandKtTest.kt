package kgen

import kgen.utility.runShellCommand
import kgen.utility.runSimpleCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

internal class RunCommandKtTest {

    @Test
    fun runCommand() {
        assertEquals(
            "this is a test\n",
            "echo this is a test".runSimpleCommand()
        )
        assertEquals("3", "echo a b c | wc -w".runShellCommand(echoOutputs = true).trim())
    }

    @Test
    fun runBogusCommand() {
        assertFailsWith<RuntimeException> {
            "cat fileDoesNotExist".runShellCommand()
        }
    }

}