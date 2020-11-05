package de.quantummaid.awswebsocketdemo.usecases

private const val MIN_LENGTH = 3
private const val MAX_LENGTH = 70

data class ClientGroup(val value: String) {
    init {
        if (value.isBlank()) {
            throw ValidationException("clientGroup must not be blank")
        }
        if (value.length < MIN_LENGTH) {
            throw ValidationException("clientGroup must be at least $MIN_LENGTH characters long")
        }
        if (value.length > MAX_LENGTH) {
            throw ValidationException("clientGroup must not be longer than $MAX_LENGTH characters")
        }
    }
}
