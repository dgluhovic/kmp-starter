package com.kmp_starter.backend

import com.kmp_starter.backend.database.Addresses
import com.kmp_starter.data.Address
import com.kmp_starter.data.User
import com.kmp_starter.data.UserRegistration
import com.kmp_starter.backend.database.DatabaseFactory.dbQuery
import com.kmp_starter.backend.database.DatabaseFactory.execAndMap
import com.kmp_starter.backend.database.RawSql
import com.kmp_starter.backend.database.Users
import com.kmp_starter.backend.database.toFullUser
import com.kmp_starter.data.SearchResponse
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import org.postgis.Point

object UserService {
    suspend fun registerUser(userRegistration: UserRegistration) = dbQuery {
        val count = Users.select { Users.email.eq(userRegistration.email) }.count()

        if (count == 0) {
            val uId = Users.insert {
                it[email] = userRegistration.email
                it[password] = userRegistration.password
                it[name] = userRegistration.name
            } get Users.id

            val aId = Addresses.insert {
                it[userId] = uId
                userRegistration.address.let { a ->
                    it[street] = a.street
                    it[state] = a.state
                    it[postalCode] = a.postalCode
                    it[location] = Point(a.location.latitude, a.location.longitude)
                    it[isPrimary] = true
                }
            } get Addresses.id

            val user = User(
                id = uId,
                name = userRegistration.name,
                address = userRegistration.address.copy(id = aId),
                createdAt = DateTime.now().millis,
                updatedAt = DateTime.now().millis
            )

            HttpStatusCode.OK to user
        } else {
            HttpStatusCode.Conflict to null
        }
    }

    suspend fun getFullUserByEmail(email: String) = dbQuery {
        getFullUser { Users.email.eq(email) }
    }

    suspend fun getFullUserById(id: Long) = dbQuery {
        getFullUser { Users.id.eq(id) }
    }

    suspend fun getFullUsersWithinRadius(userId: Long, distanceMeters: Double) =
        RawSql.UsersWithinDistance(userId, distanceMeters)?.let {
            SearchResponse(execAndMap(it.stmt, it.transform))
        }


    private suspend fun getFullUser(criteria: SqlExpressionBuilder.() -> Op<Boolean>) = dbQuery {
        (Users innerJoin Addresses).select{
            criteria.invoke(this) and Users.id.eq(
                Addresses.userId)
        }.singleOrNull()?.let {
            it.toFullUser()
        }
    }
}

data class FullUser(
    val id: Long,
    val name: String,
    val email: String,
    val password: String,
    val address: Address,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun user() =
        User(id, name, address, createdAt, updatedAt)
}