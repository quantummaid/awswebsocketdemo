package de.quantummaid.awswebsocketdemo.util.aws

import de.quantummaid.awswebsocketdemo.util.aws.apigateway.HttpApiInformation
import de.quantummaid.awswebsocketdemo.util.aws.apigateway.WebsocketApiInformation
import de.quantummaid.awswebsocketdemo.util.aws.apigateway.loadHttpApiInformation
import de.quantummaid.awswebsocketdemo.util.aws.apigateway.loadWebsocketApiInformation
import de.quantummaid.awswebsocketdemo.util.aws.cloudformation.CloudFormationHandler
import de.quantummaid.awswebsocketdemo.util.aws.s3.S3Handler
import de.quantummaid.awswebsocketdemo.util.BaseDirectoryFinder
import java.io.File
import java.util.*

const val BUCKET_CF_TEMPLATE = "/cf-bucket.yml"
const val LAMBDA_CF_TEMPLATE = "/cf-lambda.yml"
const val JAR = "/server/target/awswebsocketdemo-lambda.jar"
const val PREFIX = "awswebsocketdemo-tests"

const val HTTP_API_NAME = "AWS Websocket Demo Http Lambda Proxy"
const val WEBSOCKET_API_NAME = "AWS Websocket Demo WebSockets Lambda Proxy"

data class Deployment(val httpApi: HttpApiInformation, val websocketApi: WebsocketApiInformation)

fun deployStack(): Deployment {
    val stackName = deployAndReturnStackName()
    val httpApiInformation = loadHttpApiInformation("$stackName $HTTP_API_NAME")
    val websocketApiInformation = loadWebsocketApiInformation("$stackName $WEBSOCKET_API_NAME")
    return Deployment(httpApiInformation, websocketApiInformation)
}

private fun deployAndReturnStackName(): String {
    val projectBaseDirectory = BaseDirectoryFinder.findProjectBaseDirectory()

    val client = CloudFormationHandler.forNewCloudFormationClient()

    val bucketName = PREFIX + "-bucket-" + UUID.randomUUID().toString().subSequence(0, 20)
    client.deployStack(
        bucketName,
        projectBaseDirectory + BUCKET_CF_TEMPLATE,
        mapOf(
            "ArtifactBucketName" to bucketName,
            "StackIdentifier" to bucketName
        )
    )

    val artifactFile = File(projectBaseDirectory + JAR)
    val artifactKey = S3Handler.uploadToS3Bucket(bucketName, artifactFile)

    val stackName = PREFIX + "-lambda-" + UUID.randomUUID().toString().subSequence(0, 10)
    client.deployStack(
        stackName,
        projectBaseDirectory + LAMBDA_CF_TEMPLATE,
        mapOf(
            "ArtifactBucketName" to bucketName,
            "ArtifactKey" to artifactKey,
            "StackIdentifier" to stackName
        )
    )

    return stackName
}

fun cleanStacks() {
    S3Handler.emptyAllBucketsStartingWith(PREFIX)
    CloudFormationHandler.forNewCloudFormationClient().use {
        it.deleteStacksStartingWith(PREFIX)
    }
}

fun main() {
    val deployment = deployStack()
    println(deployment.httpApi.url())
    println(deployment.websocketApi.url())
    //cleanStacks()
}
