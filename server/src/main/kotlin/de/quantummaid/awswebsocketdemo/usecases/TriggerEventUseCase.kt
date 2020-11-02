package de.quantummaid.awswebsocketdemo.usecases

data class FrontendEvent(val value1: String, val value2: String, val value3: String)

interface EventDispatcher {
    fun dispatchTo(clientId: ClientId, event: FrontendEvent)
}

class TriggerEventUseCase {

    fun triggerEvent(clientId: ClientId,
                     event: FrontendEvent,
                     eventDispatcher: EventDispatcher) {
        eventDispatcher.dispatchTo(clientId, event)
    }
}