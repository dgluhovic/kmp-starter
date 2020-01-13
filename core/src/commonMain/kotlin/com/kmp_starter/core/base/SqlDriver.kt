package com.kmp_starter.core.base

import com.squareup.sqldelight.db.SqlDriver

expect fun getSqlDriver(databaseName: String): SqlDriver