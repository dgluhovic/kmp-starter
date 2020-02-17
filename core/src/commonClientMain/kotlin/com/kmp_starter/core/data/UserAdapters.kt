package com.kmp_starter.core.data


import com.kmp_starter.data.Address
import com.kmp_starter.db.Users
import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun usersAdapter(): Users.Adapter = Users.Adapter(
    addressAdapter = addressAdapter()
)

@UseExperimental(ImplicitReflectionSerializer::class)
private fun addressAdapter(): ColumnAdapter<Address, String> = object :
    ColumnAdapter<Address, String> {
    private val json = Json(JsonConfiguration.Stable)

    override fun decode(databaseValue: String): Address = json.parse(databaseValue)

    override fun encode(value: Address): String = json.stringify(value)
}