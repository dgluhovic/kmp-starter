package com.kmp_starter.backend.database

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.HoconApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.ResultSet

object DatabaseFactory {

    private lateinit var appConfig: HoconApplicationConfig
    private lateinit var dbUrl: String
    private lateinit var dbUser: String
    private lateinit var dbPassword: String

    fun init(config: Config = ConfigFactory.load()) {
        appConfig = HoconApplicationConfig(config)
        dbUrl = appConfig.property("db.jdbcUrl").getString()
        dbUser = appConfig.property("db.dbUser").getString()
        dbPassword = appConfig.property("db.dbPassword").getString()

        Database.connect(hikari())
        val flyway = Flyway.configure().dataSource(
            dbUrl,
            dbUser,
            dbPassword
        ).load()
        flyway.migrate()
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password =
            dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T): T =
        newSuspendedTransaction { block() }

    suspend fun <T:Any> execAndMap(stmt: String, transform : (ResultSet) -> T) : List<T> {
        val result = arrayListOf<T>()
        newSuspendedTransaction {
            TransactionManager.current().exec(stmt) { rs ->
                while (rs.next()) {
                    result += transform(rs)
                }
            }

        }
        return result
    }
}