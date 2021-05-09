package de.quantummaid.awswebsocketdemo.util.aws.cloudformation

import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.*
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern
import java.util.stream.Collectors

class CloudFormationHandler(private val cloudFormationClient: CloudFormationClient) : AutoCloseable {

    fun deployStack(
        stackName: String,
        pathToTemplate: String,
        stackParameters: Map<String, String>
    ): Map<String, Any> {
        try {
            return tryDeployStack(stackName, pathToTemplate, stackParameters)
        } catch (e: CloudFormationHandlerException) {
            if (e.cause is CloudFormationException) {
                val matcher = STACK_CANNOT_BE_UPDATED_REGEX.matcher(e.cause.message)
                if (matcher.find()) {
                    println("Stack $stackName is useless (it is in '${matcher.group("state")}' state)")
                    deleteStack(stackName)
                    return tryDeployStack(stackName, pathToTemplate, stackParameters)
                }
            }
            throw IllegalStateException(e)
        }
    }

    fun tryDeployStack(
        stackName: String,
        pathToTemplate: String,
        stackParameters: Map<String, String>
    ): Map<String, Any> {
        try {
            createStack(stackName, pathToTemplate, stackParameters)
        } catch (e: AlreadyExistsException) {
            println("Stack $stackName already exists, updating instead.")
            updateStack(stackName, pathToTemplate, stackParameters)
        }
        val describeStacksResponse = cloudFormationClient.describeStacks { it.stackName(stackName) }
        val stack = describeStacksResponse.stacks()[0]
        val outputs = stack.outputs()
            .map { it.outputKey() to it.outputValue() }
            .toMap()
        println("deployed stack with outputs: $outputs")
        return outputs
    }

    fun createStack(
        stackIdentifier: String,
        pathToTemplate: String,
        stackParameters: Map<String, String>
    ) {
        println("Deploying stack $stackIdentifier...")
        val templateBody = fileToString(pathToTemplate)
        val createStackRequest: CreateStackRequest = CreateStackRequest.builder()
            .stackName(stackIdentifier)
            .capabilities(Capability.CAPABILITY_NAMED_IAM)
            .templateBody(templateBody)
            .parameters(
                stackParameters.entries
                    .stream()
                    .map {
                        Parameter.builder()
                            .parameterKey(it.key)
                            .parameterValue(it.value)
                            .build()
                    }.collect(Collectors.toList())
            )
            .build()
        CloudFormationWaiter.createStackSync(cloudFormationClient, createStackRequest)
        println("Created stack $stackIdentifier.")
    }

    fun updateStack(
        stackIdentifier: String,
        pathToTemplate: String,
        stackParameters: Map<String, String>
    ) {
        println("Updating stack $stackIdentifier...")
        val templateBody = fileToString(pathToTemplate)
        val updateStackRequest: UpdateStackRequest = UpdateStackRequest.builder()
            .stackName(stackIdentifier)
            .capabilities(Capability.CAPABILITY_NAMED_IAM)
            .templateBody(templateBody)
            .parameters(
                stackParameters.entries.stream().map { kv: Map.Entry<String, String> ->
                    Parameter.builder()
                        .parameterKey(kv.key)
                        .parameterValue(kv.value)
                        .build()
                }.collect(Collectors.toList())
            )
            .build()
        try {
            CloudFormationWaiter.updateStackSync(cloudFormationClient, updateStackRequest)
            println("Updated stack $stackIdentifier.")
        } catch (e: CloudFormationException) {
            val message = e.message
            if (message!!.contains("No updates are to be performed.")) {
                println("Stack $stackIdentifier was already up to date.")
            } else {
                throw CloudFormationHandlerException(
                    "Exception thrown during update of stack $stackIdentifier",
                    e
                )
            }
        }
    }

    fun deleteStacksStartingWith(stackPrefix: String) {
        val listStacksResponse = cloudFormationClient.listStacks()
        listStacksResponse.stackSummaries().stream()
            .filter { stack: StackSummary ->
                stack.stackStatus() == StackStatus.CREATE_COMPLETE ||
                        stack.stackStatus() == StackStatus.UPDATE_COMPLETE
            }
            .map { obj: StackSummary -> obj.stackName() }
            .filter { stackName -> stackName.startsWith(stackPrefix) }
            .forEach { stackName -> deleteStack(stackName) }
    }

    internal fun deleteStack(stackName: String) {
        println("Deleting stack $stackName...")
        val deleteStackRequest = DeleteStackRequest.builder()
            .stackName(stackName)
            .build()
        CloudFormationWaiter.deleteStackSync(cloudFormationClient, deleteStackRequest)
        println("Deleted stack $stackName.")
    }

    override fun close() {
        cloudFormationClient.close()
    }

    companion object {
        val STACK_CANNOT_BE_UPDATED_REGEX: Pattern =
            Pattern.compile("is in (?<state>[A-Z_]+) state and can not be updated")

        fun forNewCloudFormationClient(): CloudFormationHandler {
            return forExistingCloudFormationClient(CloudFormationClient.create())
        }

        fun forExistingCloudFormationClient(client: CloudFormationClient): CloudFormationHandler {
            return CloudFormationHandler(client)
        }

        private fun fileToString(filePath: String?): String {
            val contentBuilder = StringBuilder()
            try {
                Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)
                    .use { stream -> stream.forEach { s: String? -> contentBuilder.append(s).append("\n") } }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return contentBuilder.toString()
        }
    }
}
