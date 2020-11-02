package de.quantummaid.awswebsocketdemo.util

import de.quantummaid.awswebsocketdemo.WAIT_TIME

fun waitFor(condition: () -> Boolean) {
    for (x in 0..WAIT_TIME) {
        if (condition.invoke()) {
            break
        }
        Thread.sleep(1000)
    }
}