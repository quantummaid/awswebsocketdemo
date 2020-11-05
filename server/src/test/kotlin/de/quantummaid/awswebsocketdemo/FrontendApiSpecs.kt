package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.util.waitFor
import de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath
import de.quantummaid.httpmaid.client.HttpMaidClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

const val WAIT_TIME = 60

data class Clients(
        val httpClient: HttpMaidClient,
        val websocketClient: HttpMaidClient
)

abstract class FrontendApiSpecs {

    @Test
    fun eventsCanBeTriggered(clients: Clients) {
        val clientId = createClientId()
        var websocketResponse: String? = null

        clients.websocketClient.openWebsocket(
                {
                    websocketResponse = it
                },
                {},
                {},
                mapOf("clientId" to listOf(clientId)),
                mapOf()
        )

        val response = clients.httpClient.issue(
                aPostRequestToThePath("/trigger_event").withTheBody("""
                {
                    "clientId": "$clientId",
                    "event": {
                        "value1": "qwer",
                        "value2": "asdf",
                        "value3": "yxcv"
                    }
                }
            """.trimIndent())
        )

        Assertions.assertTrue(200 == response.statusCode) { response.statusCode.toString() }

        waitFor { websocketResponse != null }

        Assertions.assertTrue(websocketResponse!!.contains(""""value1":"qwer"""")) { websocketResponse }
        Assertions.assertTrue(websocketResponse!!.contains(""""value2":"asdf"""")) { websocketResponse }
        Assertions.assertTrue(websocketResponse!!.contains(""""value3":"yxcv"""")) { websocketResponse }
    }

    @Test
    fun websocketsCanBeDisconnected(clients: Clients) {
        val clientId = createClientId()
        var closed = false

        clients.websocketClient.openWebsocket(
                {},
                {
                    closed = true
                },
                {},
                mapOf("clientId" to listOf(clientId)),
                mapOf()
        )

        val response = clients.httpClient.issue(
                aPostRequestToThePath("/disconnect_everyone")
        )

        Assertions.assertTrue(200 == response.statusCode) { response.statusCode.toString() }

        waitFor { closed }

        Assertions.assertTrue(closed)
    }

    @Test
    fun clientIdCannotBeTooSmall(clients: Clients) {
        val clientId = "a"
        val response = clients.httpClient.issue(
                aPostRequestToThePath("/trigger_event").withTheBody("""
                {
                    "clientId": "$clientId",
                    "event": {
                        "value1": "qwer",
                        "value2": "asdf",
                        "value3": "yxcv"
                    }
                }
            """.trimIndent())
        )

        Assertions.assertTrue(400 == response.statusCode) { response.statusCode.toString() }
        Assertions.assertTrue("""{"errors":[{"path":"clientId","message":"clientId must be at least 3 characters long"}]}""" == response.body) { response.body }
    }

    @Test
    fun clientIdCannotBeTooBig(clients: Clients) {
        val clientId = "a".repeat(100)
        val response = clients.httpClient.issue(
                aPostRequestToThePath("/trigger_event").withTheBody("""
                {
                    "clientId": "$clientId",
                    "event": {
                        "value1": "qwer",
                        "value2": "asdf",
                        "value3": "yxcv"
                    }
                }
            """.trimIndent())
        )

        Assertions.assertTrue(400 == response.statusCode) { response.statusCode.toString() }
        Assertions.assertTrue("""{"errors":[{"path":"clientId","message":"clientId must not be longer than 70 characters"}]}""" == response.body) { response.body }
    }

    @Test
    fun clientIdCannotBeBlank(clients: Clients) {
        val clientId = "     "
        val response = clients.httpClient.issue(
                aPostRequestToThePath("/trigger_event").withTheBody("""
                {
                    "clientId": "$clientId",
                    "event": {
                        "value1": "qwer",
                        "value2": "asdf",
                        "value3": "yxcv"
                    }
                }
            """.trimIndent())
        )

        Assertions.assertTrue(400 == response.statusCode) { response.statusCode.toString() }
        Assertions.assertTrue("""{"errors":[{"path":"clientId","message":"clientId must not be blank"}]}""" == response.body) { response.body }
    }

    @Test
    fun messageAndMotivationCannotBeNull(clients: Clients) {
        val response = clients.httpClient.issue(
                aPostRequestToThePath("/broadcast_event").withTheBody("""
                {
                    "clientGroup": "foo",
                    "event": {
                    }
                }
            """.trimIndent()))

        Assertions.assertTrue(400 == response.statusCode) { response.statusCode.toString() }
        Assertions.assertEquals("""{"errors":[{"path":"event","message":"message is required"},{"path":"event","message":"motivation is required"}]}""", response.body)
    }
}

private fun createClientId(): String {
    return UUID.randomUUID().toString()
}
