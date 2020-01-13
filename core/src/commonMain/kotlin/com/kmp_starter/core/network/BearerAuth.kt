package com.kmp_starter.app.com.kmp_starter.common.network

import com.kmp_starter.core.data.hasToken
import com.kmp_starter.core.data.token
import com.russhwolf.settings.Settings
import io.ktor.client.features.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.util.*
import kotlinx.io.charsets.*
import kotlinx.io.core.*

/**
 * Add [BasicAuthProvider] to client [Auth] providers.
 */
fun Auth.bearer(block: BearerAuthConfig.() -> Unit) {
    with(BearerAuthConfig().apply(block)) {
        providers.add(
            BearerAuthProvider(
                settings,
                realm,
                sendWithoutRequest
            )
        )
    }
}

/**
 * [BasicAuthProvider] configuration.
 */
class BearerAuthConfig {

    lateinit var settings: Settings
    /**
     * Optional: current provider realm
     */
    var realm: String? = null

    /**
     * Send credentials in without waiting for [HttpStatusCode.Unauthorized].
     */
    var sendWithoutRequest: Boolean = false
}

/**
 * Client basic authentication provider.
 */
class BearerAuthProvider(
    private val settings: Settings,
    private val realm: String? = null,
    override val sendWithoutRequest: Boolean = false
) : AuthProvider {
    private val defaultCharset = Charsets.UTF_8

    override fun isApplicable(auth: HttpAuthHeader): Boolean {
        if (!settings.hasToken) return false

        if (realm != null) {
            if (auth !is HttpAuthHeader.Parameterized) return false
            return auth.parameter("realm") == realm
        }

        return true
    }

    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.headers[HttpHeaders.Authorization] = constructAuthValue()
    }

    @UseExperimental(InternalAPI::class)
    internal fun constructAuthValue(): String {
        return "Bearer ${settings.token}"
    }
}