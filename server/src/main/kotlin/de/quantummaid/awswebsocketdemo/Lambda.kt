package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.infrastructure.DynamoDbEventRepository.Companion.createDynamoDbEventRepository
import de.quantummaid.awswebsocketdemo.usecases.EventRepository
import de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository
import de.quantummaid.httpmaid.websockets.WebsocketConfigurators
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry
import de.quantummaid.injectmaid.api.ReusePolicy.EAGER_SINGLETON
import de.quantummaid.quantummaid.integrations.monolambda.MonoLambda

class Lambda {
    fun handleRequest(event: Map<String?, Any?>?) = MONO_LAMBDA.handleRequest(event)

    companion object {
        private val MONO_LAMBDA = createMonoLambda()

        fun createMonoLambda(): MonoLambda {
            val websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE")
            val eventRepositoryTable = System.getenv("EVENT_REPOSITORY_TABLE")
            val region = System.getenv("AWS_REGION")
            return MonoLambda.aMonoLambdaInRegion(region)
                    .withHttpMaid {
                        configureHttpMaid(it)
                        val dynamoDbRepository = DynamoDbRepository.dynamoDbRepository(websocketRegistryTable, "id")
                        val websocketRegistry: WebsocketRegistry = DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry(dynamoDbRepository)
                        it.configured(WebsocketConfigurators.toUseWebsocketRegistry(websocketRegistry))
                    }
                    .withInjectMaid {
                        it.withCustomType(EventRepository::class.java, {
                            createDynamoDbEventRepository(eventRepositoryTable)
                        }, EAGER_SINGLETON)
                    }
                    .build()
        }
    }
}
