package hotkitchen.user

import hotkitchen.common.*
import hotkitchen.profile.*
import hotkitchen.plugins.generateToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.addUserRoutes() = run {
    post("/signup") {
        val body = call.receive<NewUserDTO>()

        try {
            require(body.email.isEmailValid()) { "Invalid email" }
            require(body.password.isPasswordValid()) { "Invalid password" }

            transaction {
                val matches = UserEntity.find { UserTable.email eq body.email }
                if (matches.empty()) {
                    UserEntity.new {
                        email = body.email
                        password = body.password
                        userType = body.userType
                    }.let {
                        ProfileEntity.new {
                            name = ""
                            address = ""
                            phone = ""
                            user = it
                        }
                    }
                } else {
                    throw UserAlreadyExistsException()
                }
            }

            val token = generateToken(listOf("email" to body.email, "userType" to body.userType.toString()))
            call.respond(hashMapOf("token" to token))

        } catch (e: UserAlreadyExistsException) {
            call.response.status(HttpStatusCode.Forbidden)
            call.respondText("""{"status":"User already exists"}""")
        } catch (e: IllegalArgumentException) {
            call.response.status(HttpStatusCode.Forbidden)
            call.respondText("""{"status":"${e.message}"}""")
        }

    }

    post("/signin") {
        val body = call.receive<ExistingUserDTO>()

        try {
            val entity = transaction {
                val matches = UserEntity.find { UserTable.email eq body.email }

                if (matches.empty()) {
                    throw UserDoesNotExistException()
                }

                if (matches.first().password != body.password) {
                    throw WrongPasswordException()
                }
                return@transaction matches.first()
            }
            val token = generateToken(listOf("email" to body.email, "userType" to entity.userType.toString()))
            call.respond(hashMapOf("token" to token))
        } catch (e: WrongPasswordException) {
            call.response.status(HttpStatusCode.Forbidden)
            call.respondText("""{"status":"Invalid email or password"}""")
        } catch (e: UserDoesNotExistException) {
            call.response.status(HttpStatusCode.Forbidden)
            call.respondText("""{"status":"Invalid email or password"}""")
        }
    }
}
