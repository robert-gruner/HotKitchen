package hotkitchen.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import hotkitchen.category.CategoryTable
import hotkitchen.meal.MealTable
import hotkitchen.orders.OrderTable
import hotkitchen.profile.ProfileTable
import hotkitchen.user.UserTable
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase(): Database {
    val postgresUrl = environment.config.property("postgres.url").getString()
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql:$postgresUrl"
        driverClassName = "org.postgresql.Driver"
        username = environment.config.property("postgres.user").getString()
        password = environment.config.property("postgres.password").getString()
    }

    val connection by lazy {
        Database.connect(HikariDataSource(config))
    }

    transaction(connection) {
        SchemaUtils.create(UserTable, ProfileTable, MealTable, CategoryTable, OrderTable)
    }

    return connection
}
