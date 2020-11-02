package de.quantummaid.awswebsocketdemo

import de.quantummaid.awswebsocketdemo.CommandLineInterface.Companion.startCommandLineInterface
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(name = "democlient.sh",
        mixinStandardHelpOptions = true,
        version = ["democlient 1.0"],
        description = ["Connects to a websocket server."])
class DemoClient : Callable<Int> {

    @Parameters(index = "0", description = ["URL"])
    lateinit var url: String

    @Parameters(index = "1", description = ["client id"])
    lateinit var clientId: String

    override fun call(): Int {
        startCommandLineInterface(url, clientId)
        Thread.sleep(1000 * 60 * 60)
        return 0
    }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(DemoClient()).execute(*args))