package de.quantummaid.awswebsocketdemo.usecases

class ValidationException(message: String) : Exception(message)

const val MIN_LENGTH = 3
const val MAX_LENGTH = 70

data class ClientId(val value: String) {
    init {
        if (value.isBlank()) {
            throw ValidationException("clientId must not be blank")
        }
        if (value.length < MIN_LENGTH) {
            throw ValidationException("clientId must be at least $MIN_LENGTH characters long")
        }
        if (value.length > MAX_LENGTH) {
            throw ValidationException("clientId must not be longer than $MAX_LENGTH characters")
        }
    }
}
