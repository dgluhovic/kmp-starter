package com.kmp_starter.backend.database

import com.kmp_starter.data.Address
import com.kmp_starter.data.LatLng
import com.kmp_starter.data.SearchResult
import com.kmp_starter.data.User
import java.sql.ResultSet

private const val CA_LAT = "lat"
private const val CA_LON = "lon"
private const val CA_DISTANCE_METERS = "distance_meters"

abstract sealed class RawSql<T> {
    abstract val stmt: String
    abstract val transform : (ResultSet) -> T

    data class UsersWithinDistance(val userId: Long, val distanceMeters: Double) : RawSql<SearchResult>() {
        override val stmt: String
            get() = "select u.*, a2.*, " +
                    "ST_X(a2.${Addresses.location.name}) as $CA_LAT, " +
                    "ST_Y(a2.${Addresses.location.name}) as $CA_LON, " +
                    "ST_DistanceSphere(geometry(a1.${Addresses.location.name}), geometry(a2.${Addresses.location.name})) as $CA_DISTANCE_METERS " +
                    "FROM addresses a1, addresses a2, users u " +
                    "where ST_DWithin(a1.location::geography,a2.location::geography,$distanceMeters) " +
                    "and u.id = a2.user_id " +
                    "and a1.is_primary = TRUE " +
                    "and a2.is_primary = TRUE " +
                    "and a1.id = $userId and a2.id != $userId;"

        override val transform: (ResultSet) -> SearchResult
            get() = { rs ->
                SearchResult(
                    user = User(
                        id = rs.getLong(Users.id.name),
                        name = rs.getString(Users.name.name),
                        address = Address(
                            id = rs.getLong(Addresses.id.name),
                            street = rs.getString(Addresses.street.name),
                            city = rs.getString(Addresses.city.name),
                            state = rs.getString(Addresses.state.name),
                            postalCode = rs.getString(Addresses.postalCode.name),
                            country = rs.getString(Addresses.country.name),
                            createdAt = rs.getDate(Addresses.createdAt.name).time,
                            updatedAt = rs.getDate(Addresses.updatedAt.name).time,
                            primary = rs.getBoolean(Addresses.isPrimary.name),
                            location = LatLng(
                                latitude = rs.getDouble(CA_LAT),
                                longitude = rs.getDouble(CA_LON)
                            )
                        ),
                        createdAt = rs.getDate(Users.createdAt.name).time,
                        updatedAt = rs.getDate(Users.updatedAt.name).time
                    ),
                    distanceMeters = rs.getDouble(CA_DISTANCE_METERS)
                )
            }
    }
}