package de.quantummaid.awswebsocketdemo.util.aws.s3

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern

/**
 * Source: https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 */
class Md5Checksum internal constructor(value: String) {
    val value: String

    companion object {
        const val MD5_STRING_PATTERN = "[0-9a-fA-F]{32}"
        private val VALID_MD5_STRING = Pattern.compile(MD5_STRING_PATTERN).asMatchPredicate()

        fun ofFile(file: File): Md5Checksum {
            val b = md5ChecksumBytesOf(file.absolutePath)
            val hexChecksum = b.joinToString("") { "%02x".format(it) }
            return Md5Checksum(hexChecksum)
        }

        private fun md5ChecksumBytesOf(filename: String): ByteArray {
            try {
                FileInputStream(filename).use { fis ->
                    val buffer = ByteArray(4 * 1024)
                    val complete = MessageDigest.getInstance("MD5")
                    var numRead: Int
                    do {
                        numRead = fis.read(buffer)
                        if (numRead > 0) {
                            complete.update(buffer, 0, numRead)
                        }
                    } while (numRead != -1)
                    val digest = complete.digest()
                    return digest
                }
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }
    }

    init {
        if (value.isBlank()) {
            throw IllegalArgumentException("md5 checksum cannot be blank")
        }
        require(VALID_MD5_STRING.test(value)) {
            "md5 checksum '$value' must match pattern '$MD5_STRING_PATTERN'"
        }
        this.value = value.toLowerCase()
    }
}
