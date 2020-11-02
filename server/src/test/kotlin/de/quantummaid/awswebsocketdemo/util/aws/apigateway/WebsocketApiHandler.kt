package de.quantummaid.awswebsocketdemo.util.aws.apigateway

import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client
import software.amazon.awssdk.services.apigatewayv2.model.Api
import software.amazon.awssdk.services.apigatewayv2.model.GetStagesRequest

data class WebsocketApiInformation(
    private val apiId: String,
    private val region: String,
    private val stageName: String
) {
    fun host(): String {
        return String.format("%s.execute-api.%s.amazonaws.com", apiId, region)
    }

    fun basePath(): String {
        return String.format("/%s", stageName)
    }

    fun url(): String {
        return "wss://${host()}${basePath()}"
    }
}

fun loadWebsocketApiInformation(apiName: String): WebsocketApiInformation {
    ApiGatewayV2Client.create().use { apiGatewayV2Client ->
        val api = apiByName(apiName, apiGatewayV2Client)
        val apiId = api.apiId()
        val endpoint = api.apiEndpoint()
        val region = endpoint.split("\\.".toRegex()).toTypedArray()[2]
        val stageName = stageNameByApi(api, apiGatewayV2Client)
        return WebsocketApiInformation(apiId, region, stageName)
    }
}

private fun apiByName(
    apiName: String,
    apiGatewayV2Client: ApiGatewayV2Client
): Api {
    val apis = apiGatewayV2Client.apis
    return apis.items().first { api: Api -> apiName == api.name() }
}

private fun stageNameByApi(
    api: Api,
    apiGatewayV2Client: ApiGatewayV2Client
): String {
    val apiId = api.apiId()
    val websocketStages = apiGatewayV2Client.getStages(
        GetStagesRequest.builder()
            .apiId(apiId)
            .build()
    )
    if (websocketStages.items().size != 1) {
        throw UnsupportedOperationException()
    }
    val websocketStage = websocketStages.items()[0]
    return websocketStage.stageName()
}
