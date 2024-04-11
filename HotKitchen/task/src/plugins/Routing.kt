package hotkitchen.plugins

import hotkitchen.category.addCategoryRoutes
import hotkitchen.meal.addMealRoutes
import hotkitchen.orders.addOrderRoutes
import hotkitchen.profile.addProfileRoutes
import hotkitchen.user.addUserRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authenticate {
            get("/validate") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                val userType = principal.payload.getClaim("userType").asString()
                call.respondText("Hello, $userType $email")
            }
        }

        addUserRoutes()
        addProfileRoutes()
        addMealRoutes()
        addCategoryRoutes()
        addOrderRoutes()
    }
}

