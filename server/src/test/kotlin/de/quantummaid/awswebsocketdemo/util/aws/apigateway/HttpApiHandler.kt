package de.quantummaid.awswebsocketdemo.util.aws.apigateway

import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client
import software.amazon.awssdk.services.apigatewayv2.model.Api

class HttpApiInformation(
    private val apiId: String,
    private val region: String
) {

    fun host(): String {
        return "$apiId.execute-api.$region.amazonaws.com"
    }

    fun url(): String {
        return "https://${host()}/"
    }
}

fun loadHttpApiInformation(apiName: String): HttpApiInformation {
    ApiGatewayV2Client.create().use { apiGatewayV2Client ->
        val api = apiByName(apiName, apiGatewayV2Client)
        val apiId = api.apiId()
        val endpoint = api.apiEndpoint()
        val region = endpoint.split("\\.".toRegex()).toTypedArray()[2]
        return HttpApiInformation(apiId, region)
    }
}

private fun apiByName(
    apiName: String,
    apiGatewayV2Client: ApiGatewayV2Client
): Api {
    val apis = apiGatewayV2Client.apis
    return apis.items()
        .also { println(it) }
        .first { it.name() == apiName }
}
