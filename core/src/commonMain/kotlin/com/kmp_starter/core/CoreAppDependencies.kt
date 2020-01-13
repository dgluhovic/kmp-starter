package com.kmp_starter.core

import com.kmp_starter.app.com.kmp_starter.common.network.bearer
import com.kmp_starter.data.UserRegistration
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.features.*
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

interface ClientRequestErrorHandler {
    fun logout()
}

data class Configuration(
    val apiUrl: String
)

fun httpClient(
    settings: Settings,
    errorHandler: ClientRequestErrorHandler
) = HttpClient {
    expectSuccess = false

    HttpResponseValidator {
        validateResponse { response ->
            val statusCode = response.status.value
            when (statusCode) {
                401 -> {
                    errorHandler.logout()
                    throw ClientRequestException(response)
                }
                in 300..399 -> throw RedirectResponseException(response)
                in 400..499 -> throw ClientRequestException(response)
                in 500..599 -> throw ServerResponseException(response)
            }

            if (statusCode >= 600) {
                throw ResponseException(response)
            }
        }
    }

    install(JsonFeature) {
        serializer = KotlinxSerializer(Json(JsonConfiguration(strictMode = false))).apply {
            setMapper(UserRegistration::class, UserRegistration.serializer())
        }
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    install(Auth) {
        bearer {
            this.settings = settings
            sendWithoutRequest = true
        }
    }
}