package de.quantummaid.awswebsocketdemo.usecases

class ValidationException(val messages: List<String>) : Exception(messages.joinToString()) {
    constructor(message: String) : this(listOf(message))
}
