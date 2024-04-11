package hotkitchen.meal

import hotkitchen.common.MealDoesNotExistException
import hotkitchen.plugins.checkForUserType
import hotkitchen.user.UserType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.addMealRoutes() = run {
    authenticate {
        post("/meals") {
            val body = call.receive<MealDTO>()

            try {
                checkForUserType(UserType.STAFF)

                transaction {
                    MealEntity.new(body.id) {
                        title = body.title
                        price = body.price
                        imageUrl = body.imageUrl
                        categoryIds = body.categoryIds
                    }
                }
                call.response.status(HttpStatusCode.OK)
                call.respond(body)
            } catch (e: ExposedSQLException) {
                call.response.status(HttpStatusCode.BadRequest)
            } catch (e: IllegalArgumentException) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respondText("""{"status":"Access denied"}""")
            }
        }

        get("/meals") {
            val requestId = call.request.queryParameters["id"]

            try {
                val response = transaction {
                    if (requestId != null) {
                        return@transaction MealEntity.findById(requestId.toInt()).let {
                                if (it == null) throw MealDoesNotExistException()

                                MealDTO(
                                    id = it.id.value,
                                    categoryIds = it.categoryIds,
                                    price = it.price,
                                    title = it.title,
                                    imageUrl = it.imageUrl
                                )
                            }
                    } else {
                        return@transaction MealEntity.all()
                            .map {
                                MealDTO(
                                    id = it.id.value,
                                    categoryIds = it.categoryIds,
                                    price = it.price,
                                    title = it.title,
                                    imageUrl = it.imageUrl
                                )
                            }
                    }
                }
                call.response.status(HttpStatusCode.OK)
                call.respond(response)
            } catch (e: MealDoesNotExistException) {
                call.response.status(HttpStatusCode.BadRequest)
            }
        }

    }
}
