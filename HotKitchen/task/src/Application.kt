package hotkitchen

import hotkitchen.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    configureDatabase()
    configureAuthentication()
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    configureRouting()
}
