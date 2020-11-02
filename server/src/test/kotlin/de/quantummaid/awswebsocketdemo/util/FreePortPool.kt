package de.quantummaid.awswebsocketdemo.util

import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

object FreePortPool {
    private const val START_PORT = 9000
    private const val HIGHEST_PORT = 65535
    private val currentPort = AtomicInteger(START_PORT)

    fun freePort(): Int {
        val port = currentPort.incrementAndGet()
        return if (port >= HIGHEST_PORT) {
            currentPort.set(START_PORT)
            freePort()
        } else {
            try {
                val serverSocket = ServerSocket(port)
                serverSocket.close()
                port
            } catch (ex: IOException) {
                println("port $port in use, trying next one")
                freePort()
            }
        }
    }
}
