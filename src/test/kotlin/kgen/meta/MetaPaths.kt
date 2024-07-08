package kgen.meta

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

object MetaPaths {
    private val currentDir: Path = Paths.get("").toAbsolutePath()

    val root
        get(): Path {
            var dir = currentDir
            while (dir.pathString != "/" && !dir.listDirectoryEntries()
                    .contains(Paths.get(dir.pathString, ".git"))
            ) {
                dir = dir.parent
            }

            return if (dir.pathString == "/") {
                throw RuntimeException("Could not find git root of project")
            } else {
                dir
            }
        }

    val tempPath = root.resolve("temp")
}