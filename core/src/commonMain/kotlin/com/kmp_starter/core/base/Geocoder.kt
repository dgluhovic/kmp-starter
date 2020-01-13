package com.kmp_starter.core.base

import com.kmp_starter.data.Address

interface Geocoder {
    suspend fun geocode(address: String): List<Address>
}