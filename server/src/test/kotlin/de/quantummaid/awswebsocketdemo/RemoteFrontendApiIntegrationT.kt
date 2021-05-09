package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.util.aws.cleanStacks
import de.quantummaid.awswebsocketdemo.util.aws.deployStack
import de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

const val HTTPS_PORT = 443

@ExtendWith(RemoteFrontendApiIntegrationT::class)
class RemoteFrontendApiIntegrationT : ParameterResolver, FrontendApiIntegrationT() {

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
        cleanStacks()

        val deployment = deployStack()

        val httpUrl = deployment.httpApi.replace("https://", "")
        println("http endpoint url: $httpUrl")
        val httpClient = aHttpMaidClientForTheHost(httpUrl)
            .withThePort(HTTPS_PORT)
            .viaHttps()
            .build()

        val websocketUrl = deployment.websocketApi.replace("wss://", "")
        println("websocket endpoint url: $websocketUrl")
        val websocketClient = aHttpMaidClientForTheHost(websocketUrl)
            .withThePort(HTTPS_PORT)
            .viaHttps()
            .withBasePath("/" + deployment.websocketStage)
            .build()

        clients = Clients(httpClient, websocketClient)
    }

    companion object {
        var clients: Clients? = null

        @AfterAll
        @JvmStatic
        fun clean() {
            cleanStacks()
            clients?.httpClient?.close()
            clients?.websocketClient?.close()
        }
    }
}
