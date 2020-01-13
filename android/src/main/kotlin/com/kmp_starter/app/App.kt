package com.kmp_starter.app

import android.app.Application
import android.content.Intent
import com.kmp_starter.app.com.kmp_starter.core.app
import com.kmp_starter.app.com.kmp_starter.core.initializeCoreApp
import com.kmp_starter.core.data.UserRepo
import com.kmp_starter.core.base.Geocoder
import com.kmp_starter.core.base.Sleeper
import com.kmp_starter.app.userinfo.UserInfoActivity
import com.kmp_starter.core.ClientRequestErrorHandler
import com.kmp_starter.core.Configuration
import com.kmp_starter.core.KotlinDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.kmp_starter.data.Address
import com.kmp_starter.data.LatLng
import com.russhwolf.settings.AndroidSettings
import org.kodein.di.erased.instance

private const val GEOCODE_MAX_RESULTS = 6

class App : Application(),
    ClientRequestErrorHandler {

    val geocoder by lazy { android.location.Geocoder(this) }

    //private val repo: UserRepo by  app.kodein.instance()

    override fun onCreate() {
        super.onCreate()
        initializeCoreApp(
            settings = AndroidSettings(getSharedPreferences("kmp_starter", 0)),
            geocoder = object : Geocoder {
                override suspend fun geocode(address: String): List<Address> {
                    return geocoder.getFromLocationName(address, GEOCODE_MAX_RESULTS)
                        .map {
                            Address(
                                street = "${it.subThoroughfare} ${it.thoroughfare}",
                                city = it.locality,
                                county = it.subAdminArea,
                                state = it.adminArea,
                                country = it.countryCode,
                                postalCode = it.postalCode,
                                location = LatLng(
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            )
                        }
                }

            },
            errorHandler = this,
            sleeper = object : Sleeper {
                override fun sleepThread(delay: Long) {
                    Thread.sleep(delay)
                }
            },
            sqlDriver = AndroidSqliteDriver(
                KotlinDatabase.Schema,
                applicationContext,
                "kmp_starter.db"
            ),
            configuration = Configuration(
                getString(R.string.api_url)
            )
        )
    }

    override fun logout() {
        val repo: UserRepo by  app.kodein.instance()
        repo.logout()
        startActivity(
            Intent(this, UserInfoActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}