package hotkitchen.profile

import hotkitchen.common.ProfileDoesNotExistException
import hotkitchen.common.ProfileEmptyException
import hotkitchen.common.UserDoesNotExistException
import hotkitchen.common.isEmailValid
import hotkitchen.user.UserEntity
import hotkitchen.user.UserTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.addProfileRoutes() = run {
    authenticate {
        get("/me") {
            val principal = call.principal<JWTPrincipal>()
            val email = principal!!.payload.getClaim("email").asString()

            try {
                val profileDTO = transaction {
                    val user = UserEntity.find { UserTable.email eq email }.firstOrNull()

                    if (user == null) {
                        throw UserDoesNotExistException()
                    }
                    val profile = ProfileEntity.findById(user.profile.first().id)?.load(ProfileEntity::user)
                        ?: throw ProfileDoesNotExistException()

                    if (profile.isEmpty()) {
                        throw ProfileEmptyException()
                    }

                    return@transaction ProfileDTO(
                        email = profile.user.email,
                        userType = profile.user.userType,
                        name = profile.name,
                        address = profile.address,
                        phone = profile.phone,
                    )
                }
                call.respond(profileDTO)
            } catch (e: NoSuchElementException) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("""{"status":"${e.message}"}""")
            } catch (e: ProfileEmptyException) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("""{"status":"${e.message}"}""")
            }
        }

        put("/me") {
            val principal = call.principal<JWTPrincipal>()
            val jwtEmail = principal!!.payload.getClaim("email").asString()
            val body = call.receive<ProfileDTO>()

            try {
                require(jwtEmail == body.email) { "You cannot change the profile of a different user" }
                require(body.email.isEmailValid()) { "Invalid email" }

                val profileDTO = transaction {
                    val user = UserEntity.findSingleByAndUpdate(UserTable.email eq jwtEmail) {
                        it.userType = body.userType
                    }

                    if (user == null) {
                        throw UserDoesNotExistException()
                    }

                    val profile = ProfileEntity.findByIdAndUpdate(user.profile.first().id.value) {
                        it.name = body.name
                        it.address = body.address
                        it.phone = body.phone
                    }?.load(ProfileEntity::user)
                        ?: throw ProfileDoesNotExistException()

                    return@transaction ProfileDTO(
                        email = profile.user.email,
                        userType = profile.user.userType,
                        name = profile.name,
                        address = profile.address,
                        phone = profile.phone,
                    )
                }
                call.respond(profileDTO)
            } catch (e: IllegalArgumentException) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("""{"status":"${e.message}"}""")
            } catch (e: NoSuchElementException) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("""{"status":"${e.message}"}""")
            }
        }

        delete("/me") {
            val principal = call.principal<JWTPrincipal>()
            val email = principal!!.payload.getClaim("email").asString()

            try {
                transaction {
                    UserTable.deleteWhere { UserTable.email eq email }
                        .let { if (it < 1) throw UserDoesNotExistException() }
                }
                call.response.status(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.application.environment.log.error(e.message)
                call.response.status(HttpStatusCode.NotFound)
            }
        }
    }

}
