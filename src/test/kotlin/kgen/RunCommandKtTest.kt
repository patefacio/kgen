package kgen

import kgen.utility.runShellCommand
import kgen.utility.runSimpleCommand
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.lang.RuntimeException
import kotlin.test.assertFailsWith

internal class RunCommandKtTest {

    @Test
    fun runCommand() {
        assertEquals(
            "this is a test\n",
            "echo this is a test".runSimpleCommand()
        )
        assertEquals("3", "echo a b c | wc -w".runShellCommand()?.trim())
    }

    @Test
    fun runBogusCommand() {
        assertFailsWith<RuntimeException> {
            "cat fileDoesNotExist".runShellCommand()
        }
    }

}