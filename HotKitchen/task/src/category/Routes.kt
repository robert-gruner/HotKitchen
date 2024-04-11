package hotkitchen.category

import hotkitchen.common.CategoryDoesNotExistException
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

fun Routing.addCategoryRoutes() = run {
    authenticate {
        post("/categories") {
            val body = call.receive<CategoryDTO>()

            try {
                checkForUserType(UserType.STAFF)

                transaction {
                    CategoryEntity.new(body.id) {
                        title = body.title
                        description = body.description
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

        get("/categories") {
            val requestId = call.request.queryParameters["id"]

            try {
                val response = transaction {
                    if (requestId != null) {
                        return@transaction CategoryEntity.findById(requestId.toInt()).let {
                            if (it == null) throw CategoryDoesNotExistException()

                            CategoryDTO(
                                id = it.id.value,
                                description = it.description,
                                title = it.title,
                            )
                        }
                    } else {
                        return@transaction CategoryEntity.all()
                            .map {
                                CategoryDTO(
                                    id = it.id.value,
                                    description = it.description,
                                    title = it.title,
                                )
                            }
                    }
                }
                call.response.status(HttpStatusCode.OK)
                call.respond(response)
            } catch (e: CategoryDoesNotExistException) {
                call.response.status(HttpStatusCode.BadRequest)
            }
        }

    }
}
