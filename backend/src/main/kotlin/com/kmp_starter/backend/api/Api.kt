package com.kmp_starter.backend.api


import com.kmp_starter.backend.SimpleJWT
import com.kmp_starter.backend.UserService
import com.kmp_starter.data.*
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.Logger

fun Routing.api(simpleJwt: SimpleJWT, log: Logger) {

    post(Routes.LOGIN) {
        val post = call.receive<UserRegistration>()
        val user = UserService.getFullUserByEmail(post.email)
        if (user == null || !BCrypt.checkpw(post.password, user.password)) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            call.respond(
                UserToken(
                    user.user(),
                    simpleJwt.sign(user.id)
                )
            )
        }
    }

    post(Routes.USER_REGISTRATION) {
        val userRegistration = call.receive<UserRegistration>()
        val (code, user) = UserService.registerUser(userRegistration.copy(
            password = BCrypt.hashpw(userRegistration.password, BCrypt.gensalt())))
        log.info("reg $userRegistration")
        user?.let {
            call.respond(
                UserToken(
                    it,
                    simpleJwt.sign(it.id)
                )
            )
        } ?: call.respond(code)
    }

    authenticate {
        get(Routes.ME) {
            user()?.let {
                call.respond(it)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }

    authenticate {
        get(Routes.SEARCH) {
            val id = call.principal<UserIdPrincipal>()?.name
            id?.let {
                val response = UserService.getFullUsersWithinRadius(it.toLong(), call.parameters.distanceMeters)
                call.respond(response)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}

private suspend fun PipelineContext<*, ApplicationCall>.user(): User? =
    call.principal<UserIdPrincipal>()?.let {
        val userId = it.name
        UserService.getFullUserById(userId.toLongOrNull() ?: 0)?.user()
    }

private inline val Parameters?.distanceMeters: Double get() = this?.get(PARAM_DISTANCE_METERS)?.toDouble() ?: DEFAULT_DISTANCE_METERS