package de.quantummaid.awswebsocketdemo.usecases

data class BroadcastEvent(val message: String?, val motivation: String?) {
    init {
        if (message == null) {
            throw ValidationException("message is required")
        }
        if (motivation == null) {
            throw ValidationException("motivation is required")
        }
    }
}

interface EventBroadcaster {
    fun dispatchTo(clientGroup: ClientGroup, event: BroadcastEvent)
}

class BroadcastEventUseCase {

    fun broadcastEvent(clientGroup: ClientGroup,
                       event: BroadcastEvent,
                       eventBroadcaster: EventBroadcaster) {
        eventBroadcaster.dispatchTo(clientGroup, event)
    }
}
