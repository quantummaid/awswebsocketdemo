package de.quantummaid.awswebsocketdemo.util.aws.s3

import mu.KLogger
import mu.KotlinLogging
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.File

object S3Handler {
    val log: KLogger = KotlinLogging.logger {}

    fun uploadToS3Bucket(
        bucketName: String,
        file: File
    ): String {
        val key = keyFromFile(file)
        S3Client.create().use { s3Client ->
            log.info {
                "Checking for ${file.name} (size:${humanReadableByteCount(file.length(), si = false)}," +
                    " parent:${file.parent}) at s3://$bucketName/$key..."
            }


            s3Client.listBuckets().buckets().also { print(it) }
            if (!fileNeedsUploading(bucketName, key, s3Client)) {
                log.info("${file.name} (md5: $key) already present. not uploading.")
            } else {
                log.info("${file.name} needs uploading. uploading...")
                s3Client.putObject(
                    PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                    file.toPath()
                )
                log.info("Uploaded {} to S3 object {}/{}.", file, bucketName, key)
            }
            return key
        }
    }

    @Strictfp
    fun humanReadableByteCount(
        byteCount: Long,
        si: Boolean
    ): String? {
        var bytes = byteCount
        val unit = if (si) 1000 else 1024
        val absBytes = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        if (absBytes < unit) return "$bytes B"
        var exp = (Math.log(absBytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val th = Math.ceil(Math.pow(unit.toDouble(), exp.toDouble()) * (unit - 0.05)).toLong()
        if (exp < 6 && absBytes >= th - (if (th and 0xFFF == 0xD00L) 51 else 0)) exp++
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        if (exp > 4) {
            bytes /= unit.toLong()
            exp -= 1
        }
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    private fun fileNeedsUploading(
        bucketName: String,
        key: String?,
        s3Client: S3Client
    ): Boolean {
        val objectsResponse: ListObjectsResponse = s3Client.listObjects(
            ListObjectsRequest.builder()
                .bucket(bucketName)
                .build()
        )
        return objectsResponse.contents().stream()
            .map(S3Object::key)
            .noneMatch(key::equals)
    }

    private fun keyFromFile(file: File): String {
        val newContentMD5: Md5Checksum = Md5Checksum.ofFile(file)
        return newContentMD5.value
    }

    fun emptyAllBucketsStartingWith(prefix: String) {
        S3Client.create().use { s3Client ->
            val listBucketsResponse = s3Client.listBuckets()
            listBucketsResponse.buckets()
                .map { it.name() }
                .filter { it.startsWith(prefix) }
                .forEach { deleteAllObjectsInBucket(it, s3Client) }
        }
    }

    private fun deleteAllObjectsInBucket(
        bucketName: String,
        s3Client: S3Client
    ) {
        val listObjectsResponse: ListObjectsResponse = s3Client.listObjects(
            ListObjectsRequest.builder()
                .bucket(bucketName)
                .build()
        )
        listObjectsResponse.contents().forEach { s3Object ->
            val key: String = s3Object.key()
            deleteFromS3Bucket(bucketName, key, s3Client)
        }
    }

    private fun deleteFromS3Bucket(
        bucketName: String,
        key: String,
        s3Client: S3Client
    ) {
        log.info("Deleting S3 object {}/{}...", bucketName, key)
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
        )
        log.info("Deleted S3 object {}/{}.", bucketName, key)
    }
}
