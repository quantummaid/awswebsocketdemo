package de.quantummaid.awswebsocketdemo

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketListener
import java.lang.UnsupportedOperationException

class CliWebSocketListener(val commandLineInterface: CommandLineInterface): WebSocketListener {
    private var session: Session? = null

    override fun onWebSocketConnect(session: Session) {
        this.session = session
        println("Connected.")
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        println("Connection has been closed. Reconnecting...")
        commandLineInterface.reconnect()
    }

    override fun onWebSocketError(cause: Throwable) {
        println("Connection error:")
        cause.printStackTrace()
    }

    override fun onWebSocketBinary(payload: ByteArray?, offset: Int, length: Int) {
        throw UnsupportedOperationException("Received unsupported binary input.")
    }

    override fun onWebSocketText(message: String?) {
        println("Received message: $message")
    }
}
