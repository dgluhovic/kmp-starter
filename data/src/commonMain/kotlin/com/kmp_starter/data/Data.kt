package com.kmp_starter.data

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: Long,
    val name: String,
    val address: Address,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

@Serializable
data class UserRegistration(
    val name: String,
    val email: String,
    val password: String,
    val address: Address
)

@Serializable
data class Address(
    val id: Long = 0,
    val street: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val location: LatLng = LatLng(),
    val primary: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

@Serializable
data class UserToken(
    val user: User,
    val token: String
)

@Serializable
data class SearchRequest(
    val distanceMiles: Double
)

@Serializable
data class SearchResponse(
    val users: List<User>
)

@Serializable
data class LatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

