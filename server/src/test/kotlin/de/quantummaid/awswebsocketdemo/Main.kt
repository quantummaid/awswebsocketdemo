package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.infrastructure.DynamoDbEventRepository.Companion.createDynamoDbEventRepository

fun main() {
    val eventRepository = createDynamoDbEventRepository("marcodemo-EventRepository")
    //eventRepository.store(FrontendEvent("a", "b", "c"))

    val all = eventRepository.loadAll()
    println("all = ${all}")
}
