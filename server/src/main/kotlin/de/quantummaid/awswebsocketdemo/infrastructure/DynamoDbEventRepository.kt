package de.quantummaid.awswebsocketdemo.infrastructure

import de.quantummaid.awswebsocketdemo.usecases.EventRepository
import de.quantummaid.awswebsocketdemo.usecases.FrontendEvent
import de.quantummaid.mapmaid.MapMaid
import de.quantummaid.mapmaid.dynamodb.DynamoDbMarshallerAndUnmarshaller.DYNAMODB_ATTRIBUTEVALUE
import de.quantummaid.mapmaid.dynamodb.DynamoDbMarshallerAndUnmarshaller.dynamoDbMarshallerAndUnmarshaller
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import java.util.*

private const val PRIMARY_KEY = "id"
private const val VALUE_IDENTIFIER = "value"

class DynamoDbEventRepository(private val client: DynamoDbClient,
                              private val tableName: String,
                              private val mapper: MapMaid) : EventRepository, AutoCloseable {

    companion object {
        fun createDynamoDbEventRepository(tableName: String): DynamoDbEventRepository {
            val client = DynamoDbClient.create()
            val mapMaid = MapMaid.aMapMaid()
                    .serializingAndDeserializing(FrontendEvent::class.java)
                    .withAdvancedSettings { it.usingMarshaller(dynamoDbMarshallerAndUnmarshaller()) }
                    .build()
            return DynamoDbEventRepository(client, tableName, mapMaid)
        }
    }

    override fun loadAll(): List<FrontendEvent> {
        val scanResponse = client.scan {
            it
                    .tableName(tableName)
                    .consistentRead(true)
        }
        return scanResponse.items()
                .map { it[VALUE_IDENTIFIER] }
                .map { mapper.deserialize(it, FrontendEvent::class.java, DYNAMODB_ATTRIBUTEVALUE) }
    }

    override fun store(frontendEvent: FrontendEvent) {
        val id = UUID.randomUUID().toString()
        val serializedEvent = mapper.serializeTo(frontendEvent, DYNAMODB_ATTRIBUTEVALUE)
        val map = mapOf(
                PRIMARY_KEY to AttributeValue.builder().s(id).build(),
                VALUE_IDENTIFIER to serializedEvent
        )
        client.transactWriteItems {
            it.transactItems(
                    TransactWriteItem.builder()
                            .put {
                                it
                                        .tableName(tableName)
                                        .item(map)
                            }
                            .build()
            )
        }
    }

    override fun close() {
        client.close()
    }
}

