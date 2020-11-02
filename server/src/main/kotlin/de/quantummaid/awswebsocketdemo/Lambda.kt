package de.quantummaid.awswebsocketdemo

import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint
import de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint
import de.quantummaid.httpmaid.awslambda.EventUtils.isWebSocketRequest
import de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository
import de.quantummaid.httpmaid.websockets.WebsocketConfigurators
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry

class Lambda {
    fun handleRequest(event: Map<String?, Any?>?): Map<String, Any>? {
        println("event!!!!!!!: $event")
        return if (!isWebSocketRequest(event)) {
            HTTP_ENDPOINT.delegate(event)
        } else {
            WEBSOCKET_ENDPOINT.delegate(event)
        }
    }

    companion object {
        private val HTTP_MAID = httpMaid {
            val websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE")
            val dynamoDbRepository = DynamoDbRepository.dynamoDbRepository(websocketRegistryTable, "id")
            val websocketRegistry: WebsocketRegistry = DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry(dynamoDbRepository)
            it.configured(WebsocketConfigurators.toUseWebsocketRegistry(websocketRegistry))
        }

        private val HTTP_ENDPOINT = AwsLambdaEndpoint.awsLambdaEndpointFor(HTTP_MAID)
        private val WEBSOCKET_ENDPOINT = AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor(HTTP_MAID)
    }
}
