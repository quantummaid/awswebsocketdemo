package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.util.FreePortPool
import de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost
import de.quantummaid.httpmaid.jetty.JettyEndpoint
import de.quantummaid.httpmaid.jetty.JettyWebsocketEndpoint.jettyWebsocketEndpoint
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

@ExtendWith(LocalFrontendApiSpecs::class)
class LocalFrontendApiSpecs : ParameterResolver {

    override fun supportsParameter(
        p0: ParameterContext?,
        p1: ExtensionContext?
    ): Boolean {
        return true
    }

    override fun resolveParameter(
        p0: ParameterContext?,
        p1: ExtensionContext?
    ): Any {
        if (clients == null) {
            init()
        }
        return clients!!
    }

    private fun init() {
        val port = FreePortPool.freePort()
        val httpMaid = httpMaid()
        endpoint = jettyWebsocketEndpoint(httpMaid, port)
        val client = aHttpMaidClientForTheHost("localhost")
            .withThePort(port)
            .viaHttp()
            .build()
        clients = Clients(client, client)
    }

    companion object {
        var endpoint: JettyEndpoint? = null
        var clients: Clients? = null

        @AfterAll
        @JvmStatic
        fun close() {
            endpoint?.close()
            endpoint = null
            clients?.httpClient?.close()
            clients?.websocketClient?.close()
            clients = null
        }
    }
}
