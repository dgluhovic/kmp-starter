package com.kmp_starter.core.data

import com.russhwolf.settings.Settings

private const val TOKEN = "token"

var Settings.token
    get() = getString(TOKEN)
    set(value) { putString(TOKEN, value) }

val Settings.hasToken get() = token.isNotBlank()

fun Settings.clearToken() = remove(TOKEN)