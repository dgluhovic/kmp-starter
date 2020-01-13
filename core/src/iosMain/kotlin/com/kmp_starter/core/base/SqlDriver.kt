package com.kmp_starter.core.base

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.ios.NativeSqliteDriver
import com.kmp_starter.core.KotlinDatabase

actual fun getSqlDriver(databaseName: String): SqlDriver = NativeSqliteDriver(KotlinDatabase.Schema, databaseName)