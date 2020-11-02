package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.CommandLineInterface.Companion.startCommandLineInterface
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI

class CommandLineInterface(val url: String,
                           val clientId: String) : AutoCloseable {
    private var client: WebSocketClient? = null

    fun reconnect() {
        close()
        connect()
    }

    fun connect() {
        client = WebSocketClient()
        client?.start()
        val uri = URI("$url?clientId=$clientId")
        //val uri = URI(url)
        val request = ClientUpgradeRequest()
        val cliWebSocketListener = CliWebSocketListener(this)
        client?.connect(cliWebSocketListener, uri, request)
    }


    override fun close() {
        client?.destroy()
    }

    companion object {
        fun startCommandLineInterface(url: String, clientId: String): CommandLineInterface {
            val commandLineInterface = CommandLineInterface(url, clientId)
            commandLineInterface.connect()
            return commandLineInterface
        }
    }
}


fun main() {
    startCommandLineInterface("ws://localhost:8080/", "myClient")
}