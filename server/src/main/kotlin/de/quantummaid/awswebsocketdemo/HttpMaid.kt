package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.usecases.*
import de.quantummaid.httpmaid.HttpMaidBuilder
import de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toConfigureMapMaidUsingRecipe
import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria.websocketCriteria

fun configureHttpMaid(builder: HttpMaidBuilder) {
    builder
            .post("/disconnect_everyone") { request, _ ->
                request.websockets().disconnector().disconnectAll()
            }
            .post("/trigger_event", TriggerEventUseCase::class.java)
            .broadcastToWebsocketsUsing(EventDispatcher::class.java, FrontendEvent::class.java) {
                object : EventDispatcher {
                    override fun dispatchTo(clientId: ClientId, event: FrontendEvent) {
                        it.sendTo(
                                event,
                                websocketCriteria().queryParameter("clientId", clientId.value)
                        )
                    }
                }
            }
            .configured(toConfigureMapMaidUsingRecipe {
                it.withExceptionIndicatingValidationError(ValidationException::class.java)
            })
}
