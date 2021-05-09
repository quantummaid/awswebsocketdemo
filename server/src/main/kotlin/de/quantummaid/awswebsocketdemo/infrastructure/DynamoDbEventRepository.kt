package de.quantummaid.awswebsocketdemo.infrastructure

import de.quantummaid.awswebsocketdemo.usecases.EventRepository
import de.quantummaid.awswebsocketdemo.usecases.FrontendEvent
import de.quantummaid.mapmaid.MapMaid
import de.quantummaid.mapmaid.dynamodb.attributevalue.AttributeValueMarshallerAndUnmarshaller.DYNAMODB_ATTRIBUTEVALUE
import de.quantummaid.mapmaid.dynamodb.attributevalue.AttributeValueMarshallerAndUnmarshaller.attributeValueMarshallerAndUnmarshaller
import de.quantummaid.reflectmaid.ReflectMaid
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import java.util.*

private const val PRIMARY_KEY = "id"
private const val VALUE_IDENTIFIER = "value"

class RepositoryMapper(private val mapMaid: MapMaid) {

    companion object {
        fun repositoryMapper(reflectMaid: ReflectMaid): RepositoryMapper {
            val mapMaid = MapMaid.aMapMaid(reflectMaid)
                .serializingAndDeserializing(FrontendEvent::class.java)
                .withAdvancedSettings { it.usingMarshaller(attributeValueMarshallerAndUnmarshaller()) }
                .build()
            return RepositoryMapper(mapMaid)
        }
    }

    fun deserialize(attributeValue: AttributeValue?): FrontendEvent {
        return mapMaid.deserialize(attributeValue, FrontendEvent::class.java, DYNAMODB_ATTRIBUTEVALUE)
    }

    fun serialize(frontendEvent: FrontendEvent): AttributeValue {
        return mapMaid.serializeTo(frontendEvent, DYNAMODB_ATTRIBUTEVALUE)
    }
}

class DynamoDbEventRepository(
    private val client: DynamoDbClient,
    private val tableName: String,
    private val mapper: RepositoryMapper
) : EventRepository, AutoCloseable {

    companion object {
        fun createDynamoDbEventRepository(tableName: String, mapper: RepositoryMapper): DynamoDbEventRepository {
            val client = DynamoDbClient.create()
            return DynamoDbEventRepository(client, tableName, mapper)
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
            .map { mapper.deserialize(it) }
    }

    override fun store(frontendEvent: FrontendEvent) {
        val id = UUID.randomUUID().toString()
        val serializedEvent = mapper.serialize(frontendEvent)
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

