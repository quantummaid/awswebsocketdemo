package de.quantummaid.awswebsocketdemo.util.aws

import de.quantummaid.awswebsocketdemo.util.BaseDirectoryFinder
import de.quantummaid.awswebsocketdemo.util.aws.cloudformation.CloudFormationHandler
import de.quantummaid.awswebsocketdemo.util.aws.s3.S3Handler
import java.io.File
import java.util.*

const val BUCKET_CF_TEMPLATE = "/cf-bucket.yml"
const val LAMBDA_CF_TEMPLATE = "/cf-lambda.yml"
const val JAR = "/server/target/awswebsocketdemo-lambda.zip"
const val PREFIX = "awswebsocketdemo-tests"

data class Deployment(val httpApi: String, val websocketApi: String, val websocketStage: String)

fun deployStack(): Deployment {
    val deployedStack = deploy()
    val httpApiInformation = deployedStack.outputs["HttpEndpoint"] as String
    val websocketApiInformation = deployedStack.outputs["WebSocketEndpoint"] as String
    val websocketApiStage = deployedStack.outputs["WebSocketStage"] as String
    return Deployment(httpApiInformation, websocketApiInformation, websocketApiStage)
}

data class DeployedStack(val stackName: String, val outputs: Map<String, Any>)

private fun deploy(): DeployedStack {
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
    val outputs = client.deployStack(
        stackName,
        projectBaseDirectory + LAMBDA_CF_TEMPLATE,
        mapOf(
            "ArtifactBucketName" to bucketName,
            "ArtifactKey" to artifactKey,
            "StackIdentifier" to stackName
        )
    )

    return DeployedStack(stackName, outputs)
}

fun cleanStacks() {
    S3Handler.emptyAllBucketsStartingWith(PREFIX)
    CloudFormationHandler.forNewCloudFormationClient().use {
        it.deleteStacksStartingWith(PREFIX)
    }
}

fun main() {
    val deployment = deployStack()
    println(deployment.httpApi)
    println(deployment.websocketApi)
    //cleanStacks()
}
