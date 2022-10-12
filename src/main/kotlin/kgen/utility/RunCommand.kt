package kgen.utility

import kgen.kgenLogger
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun runSimpleCommand(
    workingDir: File = File("."),
    command: List<String>,
    ignoreError: Boolean = false,
    timeoutMinutes: Long = 60,
    ignoreErrors: Set<Int> = emptySet()
): String? {
    return try {
        kgenLogger.info { "Running `$command`" }
        val proc = ProcessBuilder(command)
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(timeoutMinutes, TimeUnit.MINUTES)
        if (!ignoreError && !ignoreErrors.contains(proc.exitValue()) && proc.exitValue() != 0) {
            val errorMessage = "RunSimpleCommand FAILED: exit(${proc.exitValue()}) command($command) - ${
                proc.inputStream.bufferedReader().readText()
            }"
            kgenLogger.error { "$errorMessage\n${proc.errorStream.bufferedReader().readText()}\n" }
            throw RuntimeException(errorMessage)
        }
        proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun String.runSimpleCommand(
    workingDir: File = File("."),
    ignoreError: Boolean = false,
    timeoutMinutes: Long = 60,
    ignoreErrors: Set<Int> = emptySet()
) =
    runSimpleCommand(workingDir, this.split("\\s".toRegex()), ignoreError, timeoutMinutes, ignoreErrors)

fun String.runShellCommand(
    workingDir: File = File("."),
    ignoreError: Boolean = false,
    timeoutMinutes: Long = 60,
    ignoreErrors: Set<Int> = emptySet()
) =
    runSimpleCommand(workingDir, listOf("/bin/sh", "-c", this), ignoreError, timeoutMinutes, ignoreErrors)