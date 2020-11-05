package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.CommandLineInterface.Companion.startCommandLineInterface
import de.quantummaid.awswebsocketdemo.util.FreePortPool
import de.quantummaid.httpmaid.HttpMaid.anHttpMaid
import de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath
import de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost
import de.quantummaid.httpmaid.jetty.JettyWebsocketEndpoint.jettyWebsocketEndpoint
import org.junit.jupiter.api.Test
import java.util.*

class CommandLineInterfaceSpecs {

    @Test
    fun cliCanConnect() {
        val clientId = UUID.randomUUID().toString()
        val clientGroup = "c0d3rs"

        val httpMaid = anHttpMaid()
                .post("/publish") { request, response ->
                    request.websockets().sender().sendToAll("foo")
                }
                .build()
        val port = FreePortPool.freePort()

        val httpMaidClient = aHttpMaidClientForTheHost("localhost")
                .withThePort(port)
                .viaHttp()
                .build()

        jettyWebsocketEndpoint(httpMaid, port).use {

            Thread.sleep(1000)

            val url = "ws://localhost:$port/"
            println(url)
            startCommandLineInterface(url, clientId, clientGroup)

            httpMaidClient.issue(aPostRequestToThePath("/publish"))

            Thread.sleep(1000)
        }
    }

    @Test
    fun cliCanReconnect() {
        val clientId = UUID.randomUUID().toString()
        val clientGroup = "c0d3rs"

        val httpMaid = anHttpMaid()
                .post("/publish") { request, response ->
                    request.websockets().sender().sendToAll("foo")
                }
                .post("/disconnect") { request, response ->
                    request.websockets().disconnector().disconnectAll()
                }
                .build()
        val port = FreePortPool.freePort()

        val httpMaidClient = aHttpMaidClientForTheHost("localhost")
                .withThePort(port)
                .viaHttp()
                .build()

        jettyWebsocketEndpoint(httpMaid, port).use {

            Thread.sleep(1000)

            val url = "ws://localhost:$port/"
            println(url)
            startCommandLineInterface(url, clientId, clientGroup)
            for (i in 1..10) {
                Thread.sleep(1000)
                httpMaidClient.issue(aPostRequestToThePath("/disconnect"))
                Thread.sleep(1000)
                httpMaidClient.issue(aPostRequestToThePath("/publish"))
            }

            Thread.sleep(1000)
        }
    }
}
