package de.quantummaid.awswebsocketdemo

import de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntime

val MONO_LAMBDA = Lambda.createMonoLambda()

fun main() {
    GraalVmLambdaRuntime.startGraalVmLambdaRuntime(MONO_LAMBDA::handleRequest)
}
