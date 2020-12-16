package de.quantummaid.awswebsocketdemo.usecases

data class FrontendEvent(val value1: String, val value2: String, val value3: String)

interface EventDispatcher {
    fun dispatchTo(clientId: ClientId, event: FrontendEvent)
}



interface EventRepository {
    fun loadAll(): List<FrontendEvent>

    fun store(frontendEvent: FrontendEvent)
}









class TriggerEventUseCase(private val eventRepository: EventRepository) {

    fun triggerEvent(clientId: ClientId,
                     event: FrontendEvent,
                     eventDispatcher: EventDispatcher) {
        eventDispatcher.dispatchTo(clientId, event)
        eventRepository.store(event)
    }
}

class ListEventsUseCase(private val eventRepository: EventRepository) {
    fun listEvents() = eventRepository.loadAll()
}
