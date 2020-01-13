package com.kmp_starter.core.data


import com.kmp_starter.data.User
import com.kmp_starter.data.UserRegistration
import com.kmp_starter.data.UserToken
import com.russhwolf.settings.Settings
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.Logger
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val DEFAULT_SEARCH_RADIUS_MILES = 10.0

class UserRepo(
    private val api: UserApi,
    private val settings: Settings,
    private val dao: UserDao
) {
    fun registerUser(
        userRegistration: UserRegistration
    ): Flow<User>  = flow {
        val userToken = api.registerUser(userRegistration)
        saveUserToken(userToken)
        emit(userToken.user)
    }

    fun login(
        email: String, password: String
    ): Flow<User>  = flow {
        val userToken = api.login(email, password)
        saveUserToken(userToken)
        emit(userToken.user)
    }

    fun search(
        distance: Double = DEFAULT_SEARCH_RADIUS_MILES
    ): Flow<List<User>>  = flow {
        emit(api.search(distance).users)
    }

    fun me(): Flow<User>  = flow {
        val user = dao.selectUser()

        user?.let {
            emit(it)
        }


        //if (user?.isStale) {
        if (user == null) {
            val remoteUser = api.me()
            dao.upsertUser(remoteUser)
            emit(remoteUser)
        }
    }

    fun logout() {
        dao.deleteUsers()
        settings.clearToken()
    }

    private fun saveUserToken(userToken: UserToken) {
        settings.token = userToken.token
        dao.upsertUser(userToken.user)
    }
}