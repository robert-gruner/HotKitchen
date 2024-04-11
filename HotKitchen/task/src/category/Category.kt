package hotkitchen.category

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class CategoryDTO(
    @SerialName("categoryId")
    val id: Int, val title: String, val description: String
)

object CategoryTable : IntIdTable("categories") {
    val title = varchar("title", 100)
    val description = varchar("description", 500)
}

class CategoryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CategoryEntity>(CategoryTable)

    var title by CategoryTable.title
    var description by CategoryTable.description
}
