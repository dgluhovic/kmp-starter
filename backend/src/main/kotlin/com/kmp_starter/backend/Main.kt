package com.kmp_starter.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.kmp_starter.backend.api.api
import com.kmp_starter.backend.database.DatabaseFactory
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.serialization.serialization
import io.ktor.util.error
import java.util.*

class SimpleJWT(secret: String) {
    private val validityInMs = 36_000_000_000_00
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm).build()
    fun sign(id: Long): String = JWT.create()
        .withClaim("name", id.toString())
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}

fun Application.module() {
    val simpleJwt = SimpleJWT(
        environment.config.property("jwt.secret").getString()
    )

    install(CallLogging)
    install(DefaultHeaders)
    install(ConditionalHeaders)
    install(StatusPages) {
        exception<ServiceUnavailable> { _ ->
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
        exception<BadRequest> { _ ->
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<Unauthorized> { _ ->
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<NotFound> { _ ->
            call.respond(HttpStatusCode.NotFound)
        }
        exception<SecretInvalidError> { _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<Throwable> { cause ->
            environment.log.error(cause)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    install(ContentNegotiation) {
        serialization()
    }

    install(Routing) {
        api(simpleJwt, log)
    }

    DatabaseFactory.init()
}

val Application.log get() = environment.log