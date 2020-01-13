package com.kmp_starter.backend.database

import com.kmp_starter.backend.database.Users.default
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import org.postgis.PGgeometry
import org.postgis.Point
import java.time.LocalDateTime

private const val SRID = 4326

object Users : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val createdAt = datetime("created_at")//.default(DateTime.now())
    val updatedAt = datetime("updated_at")//.default(DateTime.now())
}

object Addresses : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val userId = long("user_id") references Users.id
    val street = varchar("street", 255).nullable()
    val city = varchar("city", 255).nullable()
    val county = varchar("county", 255).nullable()
    val state = varchar("state", 255).nullable()
    val country = varchar("country", 255).nullable()
    val postalCode = varchar("postal_code", 255).nullable()
    val location = point("location")
    val isPrimary = bool("is_primary")
    val createdAt = datetime("created_at")//.default(DateTime.now())
    val updatedAt = datetime("updated_at")//.default(DateTime.now())
}


fun Table.point(name: String, srid: Int = SRID): Column<Point>
        = registerColumn(name, PointColumnType())

private class PointColumnType(val srid: Int = SRID): ColumnType() {
    override fun sqlType() = "GEOMETRY(Point, $srid)"
    override fun valueFromDB(value: Any) = if (value is PGgeometry) value.geometry else value
    override fun notNullValueToDB(value: Any): Any {
        if (value is Point) {
            if (value.srid == Point.UNKNOWN_SRID) value.srid = srid
            return PGgeometry(value)
        }
        return value
    }
}