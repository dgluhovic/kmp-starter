package com.kmp_starter.core.data

import com.kmp_starter.core.base.debug
import com.kmp_starter.data.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class UserApi(
    private val client: HttpClient,
    private val hostUrl: String
) {

    suspend fun registerUser(userRegistration: UserRegistration) = client.post<UserToken> {
        url(hostUrl)
        apiUrl(Routes.USER_REGISTRATION)
        json()
        body = userRegistration
    }

    suspend fun login(email: String, password: String) = client.post<UserToken> {
        url(hostUrl)
        apiUrl(Routes.LOGIN)
        json()
        body = UserRegistration(
            email = email,
            password = password,
            //TODO
            name = "",
            address = Address()
        )
    }

    suspend fun me() = client.get<User> {
        url(hostUrl)
        apiUrl(Routes.ME)
    }

    suspend fun search(distance: Double) = client.post<SearchResponse> {
        url(hostUrl)
        apiUrl(Routes.SEARCH)
        json()
        body = SearchRequest(distanceMiles = distance)
    }

    private fun HttpRequestBuilder.apiUrl(path: String? = null) {
        header(HttpHeaders.CacheControl, "no-cache")
        url {
            path?.let {
                encodedPath = it
            }
        }
        debug(
            UserApi::class.simpleName,
            url.encodedPath
        )
    }

    private fun HttpRequestBuilder.json() {
        contentType(ContentType.Application.Json)
    }
}