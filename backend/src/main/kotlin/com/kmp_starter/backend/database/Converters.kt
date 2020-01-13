package com.kmp_starter.backend.database

import com.kmp_starter.backend.FullUser
import com.kmp_starter.data.Address
import com.kmp_starter.data.LatLng
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toFullUser(): FullUser =
    FullUser(
        id = this[Users.id],
        name = this[Users.name],
        email = this[Users.email],
        password = this[Users.password],
        address = this.toAddress(),
        createdAt = this[Users.createdAt].millis,
        updatedAt = this[Users.updatedAt].millis
    )

fun ResultRow.toAddress(): Address =
    Address(
        id = this[Addresses.id],
        street = this[Addresses.street],
        state = this[Addresses.state],
        postalCode = this[Addresses.postalCode],
        location = LatLng(
            latitude = this[Addresses.location].x,
            longitude = this[Addresses.location].y
        ),
        primary = this[Addresses.isPrimary],
        createdAt = this[Users.createdAt].millis,
        updatedAt = this[Users.updatedAt].millis
    )