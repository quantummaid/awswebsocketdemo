package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.usecases.EventRepository
import de.quantummaid.injectmaid.api.ReusePolicy
import de.quantummaid.quantummaid.integrations.testmonolambda.TestMonoLambda
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

@ExtendWith(LocalFrontendIntegrationT::class)
class LocalFrontendIntegrationT : FrontendApiIntegrationT(), ParameterResolver {

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
        endpoint = TestMonoLambda.aTestMonoLambda()
                .withHttpMaid {
                    configureHttpMaid(it)
                }
                .withInjectMaid {
                    it.withImplementation(
                            EventRepository::class.java,
                            InMemoryEventRepository::class.java,
                            ReusePolicy.EAGER_SINGLETON)
                }
                .build()
        val client = endpoint!!.connectClient()
        clients = Clients(client, client)
    }

    companion object {
        var endpoint: TestMonoLambda? = null
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
