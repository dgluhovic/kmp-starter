package com.kmp_starter.core.data

import com.kmp_starter.core.KotlinDatabase
import com.kmp_starter.data.User
import io.ktor.util.date.GMTDate

class UserDao(database: KotlinDatabase) {

    private val db = database.userQueries

    fun upsertUser(user: User) {
        user.apply {
            db.upsertUser(id, name, address, createdAt, updatedAt)
        }
    }

    fun selectUser(): User? = db.selectAll().executeAsOneOrNull()?.let {
        User(
            id = it.id,
            name = it.name,
            address = it.address,
            createdAt = it.created_at,
            updatedAt = GMTDate().timestamp
        )
    }

    fun deleteUsers() {
        db.deleteAll()
    }
}
