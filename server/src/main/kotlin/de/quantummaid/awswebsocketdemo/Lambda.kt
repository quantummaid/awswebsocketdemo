package de.quantummaid.awswebsocketdemo

import de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository
import de.quantummaid.httpmaid.websockets.WebsocketConfigurators
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry
import de.quantummaid.quantummaid.integrations.monolambda.MonoLambda

class Lambda {
    fun handleRequest(event: Map<String?, Any?>?) = MONO_LAMBDA.handleRequest(event)

    companion object {
        private val MONO_LAMBDA = MonoLambda.aMonoLambda()
                .withHttpMaid {
                    configureHttpMaid(it)
                    val websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE")
                    val dynamoDbRepository = DynamoDbRepository.dynamoDbRepository(websocketRegistryTable, "id")
                    val websocketRegistry: WebsocketRegistry = DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry(dynamoDbRepository)
                    it.configured(WebsocketConfigurators.toUseWebsocketRegistry(websocketRegistry))
                }
                .build()
    }
}
