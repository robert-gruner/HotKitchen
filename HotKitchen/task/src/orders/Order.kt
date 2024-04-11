package hotkitchen.orders

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
enum class OrderStatus(private val status: String) {
    @SerialName("COOK")
    IN_PROGRESS("COOK"),
    @SerialName("COMPLETE")
    COMPLETE("COMPLETE");

    override fun toString(): String {return this.status}

    companion object {
        private val mapping = OrderStatus.values().associateBy(OrderStatus::status)
        fun from(status: String) = mapping[status]
    }
}

@Serializable
data class OrderDTO(
    @SerialName("orderId")
    val id: Int,
    val userEmail: String,
    val price: Float,
    val address: String,
    val mealsIds: List<Int>,
    val status: OrderStatus
)

object OrderTable: IntIdTable("orders") {
    val address = varchar("address", 200)
    val price = float("price")
    val userEmail = varchar("email", 80)
    val mealsIds = array<Int>("mealsIds")
    val status = enumerationByName("status", 12, OrderStatus::class)
}

class OrderEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<OrderEntity>(OrderTable)

    var address by OrderTable.address
    var price by OrderTable.price
    var userEmail by OrderTable.userEmail
    var mealsIds by OrderTable.mealsIds
    var status by OrderTable.status
}
