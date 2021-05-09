package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.infrastructure.DynamoDbEventRepository.Companion.createDynamoDbEventRepository
import de.quantummaid.awswebsocketdemo.infrastructure.RepositoryMapper
import de.quantummaid.awswebsocketdemo.usecases.EventRepository
import de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository
import de.quantummaid.httpmaid.awslambda.sender.apigateway.sync.ApiGatewaySyncClientFactory
import de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toUseWebsocketRegistry
import de.quantummaid.injectmaid.api.ReusePolicy.LAZY_SINGLETON
import de.quantummaid.quantummaid.integrations.monolambda.MonoLambda
import de.quantummaid.reflectmaid.ReflectMaid
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient
import java.net.URI

class Lambda {

    companion object {
        fun createMonoLambda(): MonoLambda {
            val reflectMaid = ReflectMaid.aReflectMaid()
            val region = System.getenv("AWS_REGION")
            return createMonoLambda(reflectMaid, region)
        }

        fun createMonoLambda(
            reflectMaid: ReflectMaid,
            region: String
        ): MonoLambda {
            val repositoryMapper = RepositoryMapper.repositoryMapper(reflectMaid)
            return MonoLambda.aMonoLambdaInRegion(reflectMaid, region)
                .withHttpMaid {
                    configureHttpMaid(it)
                    it.disableStartupChecks()
                    it.configured(toUseWebsocketRegistry {
                        val websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE")
                        val dynamoDbRepository = DynamoDbRepository.dynamoDbRepository(websocketRegistryTable, "id")
                        dynamoDbWebsocketRegistry(dynamoDbRepository)
                    })
                }
                .withApiGatewayClientFactory(ApiGatewaySyncClientFactory.syncApiGatewayClientFactory {
                    ApiGatewayManagementApiClient.builder().endpointOverride(URI(it)).build()
                })
                .withInjectMaid {
                    it.withCustomType(EventRepository::class.java, {
                        val eventRepositoryTable = System.getenv("EVENT_REPOSITORY_TABLE")
                        createDynamoDbEventRepository(eventRepositoryTable, repositoryMapper)
                    }, LAZY_SINGLETON)
                }
                .build()
        }
    }
}
