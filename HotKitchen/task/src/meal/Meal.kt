package hotkitchen.meal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

@Serializable
data class MealDTO(
    @SerialName("mealId")
    val id: Int,
    val title: String,
    val price: Float,
    val imageUrl: String,
    val categoryIds: List<Int>
)

object MealTable : IdTable<Int>("meals") {
    override val id = integer("id").entityId()
    val title = varchar("title", 100)
    val price = float("price")
    val imageUrl = varchar("imageUrl", 300)
    val categoryIds = array<Int>("categoryIds")

    override val primaryKey = PrimaryKey(id)
}

class MealEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MealEntity>(MealTable)

    var title by MealTable.title
    var price by MealTable.price
    var imageUrl by MealTable.imageUrl
    var categoryIds by MealTable.categoryIds
}
