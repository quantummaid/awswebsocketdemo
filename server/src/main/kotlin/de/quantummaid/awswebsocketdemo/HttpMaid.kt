package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.usecases.*
import de.quantummaid.httpmaid.HttpMaidBuilder
import de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.toConfigureMapMaidUsingRecipe
import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria.websocketCriteria
import de.quantummaid.mapmaid.mapper.deserialization.validation.ValidationError

fun configureHttpMaid(builder: HttpMaidBuilder) {
    builder
            .post("/disconnect_everyone") { request, _ ->
                request.websockets().disconnector().disconnectAll()
            }
            .post("/trigger_event", TriggerEventUseCase::class.java)
            .get("/list_events", ListEventsUseCase::class.java)
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
            .post("/broadcast_event", BroadcastEventUseCase::class.java)
            .broadcastToWebsocketsUsing(EventBroadcaster::class.java, BroadcastEvent::class.java) {
                object : EventBroadcaster {
                    override fun dispatchTo(clientGroup: ClientGroup, event: BroadcastEvent) {
                        it.sendTo(
                                event,
                                websocketCriteria().queryParameter("clientGroup", clientGroup.value)
                        )
                    }
                }
            }
            .configured(toConfigureMapMaidUsingRecipe {
                it.withExceptionIndicatingMultipleValidationErrors(ValidationException::class.java) { exception, path ->
                    exception.messages.map { ValidationError.fromStringMessageAndPropertyPath(it, path) }
                }
            })
}
