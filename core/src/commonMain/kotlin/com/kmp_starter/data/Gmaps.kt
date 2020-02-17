package com.kmp_starter.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GmapsAddressComponent(
    @SerialName("long_name") val longName: String,
    @SerialName("short_name") val shortName: String,
    @SerialName("types") val types: List<String>
)

@Serializable
data class GmapsLocation(
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double
)

@Serializable
data class GmapsGeometry(
    @SerialName("location") val location: GmapsLocation
)

@Serializable
data class GmapsGeocodeResult(
    @SerialName("address_components") val addressComponents: List<GmapsAddressComponent>,
    @SerialName("geometry") val geometry: GmapsGeometry
) {
    val streetAddress: String? get() = addressComponents.firstOrNull {
        TYPE_STREET_NUMBER in it.types
    }?.shortName?.let { streetNumber ->
        addressComponents.firstOrNull {
            TYPE_ROUTE in it.types
        }?.shortName?.let { street ->
            "$streetNumber $street"
        }
    }

    val city: String? get() = addressComponents.firstOrNull {
        TYPE_LOCALITY in it.types
    }?.shortName

    val state: String? get() = addressComponents.firstOrNull {
        TYPE_ADMIN_AREA_1 in it.types
    }?.shortName

    val country: String? get() = addressComponents.firstOrNull {
        TYPE_COUNTRY in it.types
    }?.shortName

    val postalCode: String? get() = addressComponents.firstOrNull {
        TYPE_POSTAL_CODE in it.types
    }?.shortName

    fun toAddress() = Address(
        street = streetAddress,
        city = city,
        state = state,
        country = country,
        postalCode = postalCode,
        location = LatLng(
            latitude = geometry.location.lat,
            longitude = geometry.location.lng
        )
    )

    companion object {
        const val TYPE_STREET_NUMBER = "street_number"
        const val TYPE_ROUTE = "route"
        const val TYPE_LOCALITY = "locality"
        const val TYPE_ADMIN_AREA_2 = "administrative_area_level_2"
        const val TYPE_ADMIN_AREA_1 = "administrative_area_level_1"
        const val TYPE_COUNTRY = "country"
        const val TYPE_POSTAL_CODE = "postal_code"
    }
}

@Serializable
data class GmapsGeocodeResponse(
    @SerialName("results") val results: List<GmapsGeocodeResult>
) {
    fun toAddress() = results.firstOrNull()?.toAddress()
}