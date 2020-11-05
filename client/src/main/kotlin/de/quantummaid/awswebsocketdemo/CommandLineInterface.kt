package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.CommandLineInterface.Companion.startCommandLineInterface
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI

class CommandLineInterface(private val url: String,
                           private val clientId: String,
                           private val clientGroup: String) : AutoCloseable {
    private var client: WebSocketClient? = null

    fun reconnect() {
        close()
        connect()
    }

    fun connect() {
        client = WebSocketClient()
        client?.start()
        val uri = URI("$url?clientId=$clientId&clientGroup=$clientGroup")
        val request = ClientUpgradeRequest()
        val cliWebSocketListener = CliWebSocketListener(this)
        client?.connect(cliWebSocketListener, uri, request)
    }


    override fun close() {
        client?.destroy()
    }

    companion object {
        fun startCommandLineInterface(url: String, clientId: String, clientGroup: String): CommandLineInterface {
            val commandLineInterface = CommandLineInterface(url, clientId, clientGroup)
            commandLineInterface.connect()
            return commandLineInterface
        }
    }
}


fun main() {
    startCommandLineInterface("ws://localhost:8080/", "myClient", "C0d3rs")
}
