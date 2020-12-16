package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.usecases.BroadcastEventUseCase
import de.quantummaid.awswebsocketdemo.usecases.EventRepository
import de.quantummaid.awswebsocketdemo.usecases.ListEventsUseCase
import de.quantummaid.awswebsocketdemo.usecases.TriggerEventUseCase
import de.quantummaid.awswebsocketdemo.util.FreePortPool
import de.quantummaid.httpmaid.HttpMaid.anHttpMaid
import de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost
import de.quantummaid.httpmaid.jetty.JettyEndpoint
import de.quantummaid.httpmaid.jetty.JettyWebsocketEndpoint.jettyWebsocketEndpoint
import de.quantummaid.httpmaid.usecases.UseCaseConfigurators.toCreateUseCaseInstancesUsing
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class PlaceholderInjector(val eventRepository: EventRepository) : UseCaseInstantiator {
    override fun <T : Any?> instantiate(type: Class<T>?): T {
        return when (type) {
            TriggerEventUseCase::class.java -> TriggerEventUseCase(eventRepository)
            BroadcastEventUseCase::class.java -> BroadcastEventUseCase()
            ListEventsUseCase::class.java -> ListEventsUseCase(eventRepository)
            else -> throw UnsupportedOperationException("unsupported type $type")
        } as T
    }
}

@ExtendWith(LocalFrontendApiSpecs::class)
class LocalFrontendApiSpecs : FrontendApiSpecs(), ParameterResolver {

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
        val eventRepository = InMemoryEventRepository()
        val httpMaid = anHttpMaid()
                .configured(toCreateUseCaseInstancesUsing(PlaceholderInjector(eventRepository)))
                .apply { configureHttpMaid(this) }
                .build()
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
