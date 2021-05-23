package de.quantummaid.awswebsocketdemo

import de.quantummaid.mapmaid.MapMaid
import de.quantummaid.mapmaid.builder.MapMaidBuilder
import de.quantummaid.reflectmaid.GenericType.Companion.genericType

class Response<T>(val field0: T, val field1: String)

class MyCustomPrimitive(val value: String)

fun MapMaidBuilder.withListSupport(): MapMaidBuilder {
    return this
}

fun main() {
    val mapMaid = MapMaid.aMapMaid()
        .serializing(genericType<Response<String>>())
        .serializing(genericType<Response<MyCustomPrimitive>>())
        .withListSupport()
        .build()

    mapMaid.serializeToJson(
        listOf(
            Response("a", "b"),
            Response(MyCustomPrimitive("c"), "d")
        ),
        genericType<List<Any>>()
    )
}