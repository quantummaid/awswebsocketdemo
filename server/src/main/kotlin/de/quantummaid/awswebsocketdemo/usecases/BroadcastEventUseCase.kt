package de.quantummaid.awswebsocketdemo.usecases

data class BroadcastEvent(val message: String?, val motivation: String?) {
    init {
        val validations = mutableListOf<String>()
        if (message == null) {
            validations.add("message is required")
        }
        if (motivation == null) {
            validations.add("motivation is required")
        }
        if (!validations.isEmpty()) {
            throw ValidationException(validations)
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
