package kgen.utility

import kgen.kgenLogger
import java.io.File
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.concurrent.TimeUnit

fun runCommand(
    workingDir: File = File("."),
    command: List<String>,
    ignoreError: Boolean = false,
    timeoutMinutes: Long = 60,
    ignoreErrors: Set<Int> = emptySet(),
    mergeStreams: Boolean = false,
    targetOut: Writer? = null,
    targetErr: Writer? = null,
    echoOutputs: Boolean = true
) {
    try {
        kgenLogger.info { "Running (`$command`) from `$workingDir`" }

        val procBuilder = ProcessBuilder(command)
            .directory(workingDir)

        if (mergeStreams) {
            procBuilder.redirectErrorStream(true)
        }

        val proc = procBuilder.start()

        val stdoutReader = Thread {
            proc.inputStream.bufferedReader().lines().forEach { line ->
                val withNewLine = "$line\n"
                targetOut?.write(withNewLine)
                if (echoOutputs) {
                    print(withNewLine)
                }
            }
        }

        val stderrReader = Thread {
            proc.errorStream.bufferedReader().lines().forEach { line ->
                val withNewLine = "$line\n"
                targetErr?.write(withNewLine)
                if (echoOutputs) {
                    print(withNewLine)
                }
            }
        }

        stdoutReader.start()
        stderrReader.start()
        stdoutReader.join()
        stderrReader.join()

        proc.waitFor(timeoutMinutes, TimeUnit.MINUTES)

        if (!ignoreError && !ignoreErrors.contains(proc.exitValue()) && proc.exitValue() != 0) {
            throw RuntimeException("RunSimpleCommand FAILED: exit(${proc.exitValue()}) command($command)")
        }

        targetOut?.close()
        targetErr?.close()

    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun String.runSimpleCommand(
    workingDir: File = File("."),
    ignoreError: Boolean = false,
    timeoutMinutes: Long = 60,
    ignoreErrors: Set<Int> = emptySet(),
    mergeStreams: Boolean = false,
    echoOutputs: Boolean = false
): String {

    val out = StringWriter()

    runCommand(
        workingDir,
        this.split("\\s".toRegex()),
        ignoreError,
        timeoutMinutes,
        ignoreErrors,
        mergeStreams,
        targetOut = out,
        //targetErr = targetErr,
        echoOutputs = echoOutputs

    )

    return out.toString()
}

fun String.runShellCommand(
    workingDir: File = File("."),
    ignoreError: Boolean = false,
    timeoutMinutes: Long = 60,
    ignoreErrors: Set<Int> = emptySet(),
    mergeStreams: Boolean = false,
    echoOutputs: Boolean = false
): String {
    val out = StringWriter()

    runCommand(
        workingDir,
        listOf("/bin/sh", "-c", this),
        ignoreError,
        timeoutMinutes,
        ignoreErrors,
        mergeStreams,
        targetOut = out,
        echoOutputs = echoOutputs
    )

    return out.toString()
}


fun main() {

    val x = "echo a b c | wc -w".runShellCommand()

    println("Results -> ${x.trim()}")
}
