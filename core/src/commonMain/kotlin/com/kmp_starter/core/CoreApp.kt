package com.kmp_starter.app.com.kmp_starter.core

import com.squareup.sqldelight.db.SqlDriver
import com.kmp_starter.core.ClientRequestErrorHandler
import com.kmp_starter.core.Configuration
import com.kmp_starter.core.KotlinDatabase
import com.kmp_starter.core.data.*
import com.kmp_starter.core.base.Geocoder
import com.kmp_starter.core.base.Sleeper
import com.kmp_starter.core.base.freeze
import com.kmp_starter.core.httpClient
import com.russhwolf.settings.Settings
import org.kodein.di.Kodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton
import org.kodein.di.erased.with

class CoreApp(
    settings: Settings,
    geocoder: Geocoder,
    errorHandler: ClientRequestErrorHandler,
    sqlDriver: SqlDriver,
    configuration: Configuration
) {

    val kodein = Kodein {
        bind() from singleton { settings }
        bind() from singleton { sqlDriver }
        bind() from singleton { usersAdapter() }
        bind() from singleton { KotlinDatabase(instance(), instance()) }
        bind() from singleton { UserDao(instance()) }
        bind() from singleton { errorHandler }
        bind() from singleton { geocoder }
        bind() from singleton {
            httpClient(
                instance(),
                instance()
            )
        }

        constant("apiUrl") with configuration.apiUrl
        bind() from singleton {
            UserApi(
                instance(),
                instance("apiUrl")
            )
        }
        bind() from singleton {
            UserRepo(
                instance(),
                instance(),
                instance()
            )
        }
    }
}

lateinit var app: CoreApp
    private set

/**
 * Used to initialize the singleton [CoreApp] from the platform specific application containers.
 */
fun initializeCoreApp(
    settings: Settings,
    geocoder: Geocoder,
    errorHandler: ClientRequestErrorHandler,
    sleeper: Sleeper,
    sqlDriver: SqlDriver,
    configuration: Configuration
) {
    if (!::app.isInitialized) {
        freeze(sleeper)
        app =
            CoreApp(
                settings,
                geocoder,
                errorHandler,
                sqlDriver,
                configuration
            )
    }
}