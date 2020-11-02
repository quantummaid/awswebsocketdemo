package de.quantummaid.awswebsocketdemo.util.aws.cloudformation

import java.util.function.BooleanSupplier

object Poller {
    private const val NUMBER_OF_TRIES = 60 * 1000
    private const val SLEEP_TIME = 1
    fun pollWithTimeout(condition: BooleanSupplier): Boolean {
        return pollWithTimeout(NUMBER_OF_TRIES, SLEEP_TIME, condition)
    }

    fun pollWithTimeout(
        maxNumberOfTries: Int,
        sleepTimeInMilliseconds: Int,
        condition: BooleanSupplier
    ): Boolean {
        for (i in 0 until maxNumberOfTries) {
            val conditionHasBeenFullfilled = condition.asBoolean
            if (conditionHasBeenFullfilled) {
                return true
            }
            sleep(sleepTimeInMilliseconds)
        }
        return false
    }

    fun sleep(milliseconds: Int) {
        try {
            Thread.sleep(milliseconds.toLong())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
