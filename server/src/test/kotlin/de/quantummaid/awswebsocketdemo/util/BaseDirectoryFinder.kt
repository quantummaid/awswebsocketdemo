package de.quantummaid.awswebsocketdemo.util

import java.io.File

class BaseDirectoryNotFoundException(file: String?) : RuntimeException(file)

object BaseDirectoryFinder {
    private const val PROJECT_ROOT_ANCHOR_FILENAME = ".projectrootanchor"
    private val PROJECT_ROOT_DIRECTORY = computeProjectBaseDirectory()
    fun findProjectBaseDirectory(): String {
        return PROJECT_ROOT_DIRECTORY
    }

    private fun computeProjectBaseDirectory(): String {
        val location = BaseDirectoryFinder::class.java.protectionDomain.codeSource.location.file
        var currentDirectory = File(location)
        while (!anchorFileIn(currentDirectory).exists()) {
            if (isRootDirectory(currentDirectory)) {
                throw BaseDirectoryNotFoundException(location)
            }
            currentDirectory = parentOf(currentDirectory)
        }
        return currentDirectory.absolutePath
    }

    private fun anchorFileIn(parent: File): File {
        return File(parent, PROJECT_ROOT_ANCHOR_FILENAME)
    }

    private fun isRootDirectory(f: File): Boolean {
        return f.parent == null
    }

    private fun parentOf(directory: File): File {
        return File(directory.parent)
    }
}
