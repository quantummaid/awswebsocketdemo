package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.usecases.EventRepository
import de.quantummaid.awswebsocketdemo.usecases.FrontendEvent

class InMemoryEventRepository : EventRepository {
    private val events: MutableList<FrontendEvent> = ArrayList()

    override fun loadAll(): List<FrontendEvent> {
        return events
    }

    override fun store(frontendEvent: FrontendEvent) {
        events.add(frontendEvent)
    }
}
