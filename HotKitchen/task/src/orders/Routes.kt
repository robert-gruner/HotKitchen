package hotkitchen.orders

import hotkitchen.common.OrderDoesNotExistException
import hotkitchen.meal.MealTable
import hotkitchen.user.UserEntity
import hotkitchen.user.UserTable
import hotkitchen.user.UserType
import hotkitchen.plugins.checkForUserType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun calculatePrice(meals: List<Pair<*, Float>>): Float {
    return meals.map { it.second }.sum()
}

fun Routing.addOrderRoutes() = run {
    authenticate {
        post("/order") {
            val principal = call.principal<JWTPrincipal>()
            val email = principal!!.payload.getClaim("email").asString()
            val body = call.receive<List<Int>>()

            try {
                val orderDTO = transaction {
                    val meals = MealTable.selectAll().where { MealTable.id inList body }
                        .map { it[MealTable.id] to it[MealTable.price] }

                    require (meals.isNotEmpty()) { "No matching meals" }
                    require (meals.size == body.size) { "Some meals do not exist" }

                    val user = UserEntity.find{ UserTable.email eq email }.first()

                    return@transaction OrderEntity.new {
                        userEmail = email
                        mealsIds = body
                        address = user.profile.first().address
                        status = OrderStatus.IN_PROGRESS
                        price = calculatePrice(meals)
                    }.let { OrderDTO(
                        id = it.id.value,
                        price = it.price,
                        address = it.address,
                        status = it.status,
                        mealsIds = it.mealsIds,
                        userEmail = it.userEmail
                    ) }

                }
                call.response.status(HttpStatusCode.OK)
                call.respond(orderDTO)
            } catch (e: IllegalArgumentException) {
                call.application.environment.log.error(e.message)
                call.response.status(HttpStatusCode.BadRequest)
            }
        }

        post("/order/{orderId}/markReady") {
            try {
                checkForUserType(UserType.STAFF)
                val orderId = call.parameters["orderId"]
                require(orderId != null) { "Order does not exist" }

                transaction {
                    OrderEntity.findByIdAndUpdate(orderId.toInt()) {
                        it.status = OrderStatus.COMPLETE
                    }.let {
                        if (it == null) {
                            throw OrderDoesNotExistException()
                        }
                    }
                }
                call.response.status(HttpStatusCode.OK)
            } catch (e: NumberFormatException) {
                call.response.status(HttpStatusCode.BadRequest)
            }
            catch (e: IllegalArgumentException) {
                call.application.environment.log.error(e.message)
                call.response.status(HttpStatusCode.Forbidden)
                call.respondText("""{"status":"Access denied"}""")
            } catch (e: OrderDoesNotExistException) {
                call.response.status(HttpStatusCode.BadRequest)
            }
        }

        get("/orderHistory") {
            val ordersDTO= transaction {
                OrderEntity.all().map { OrderDTO(
                    id = it.id.value,
                    price = it.price,
                    address = it.address,
                    status = it.status,
                    mealsIds = it.mealsIds,
                    userEmail = it.userEmail
                ) }
            }
            call.respond(ordersDTO)
        }

        get("/orderIncomplete") {
            val ordersDTO= transaction {
                OrderTable.selectAll().where { OrderTable.status eq OrderStatus.IN_PROGRESS }.map { OrderDTO(
                    id = it[OrderTable.id].value,
                    price = it[OrderTable.price],
                    address = it[OrderTable.address],
                    status = it[OrderTable.status],
                    mealsIds = it[OrderTable.mealsIds],
                    userEmail = it[OrderTable.userEmail]
                ) }
            }
            call.respond(ordersDTO)
        }
    }
}
