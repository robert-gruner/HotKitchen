package hotkitchen.profile

import hotkitchen.user.UserEntity
import hotkitchen.user.UserTable
import hotkitchen.user.UserType
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

@Serializable
data class ProfileDTO(
    val email: String,
    val userType: UserType,
    val address: String,
    val name: String,
    val phone: String
)

object ProfileTable : UUIDTable("profiles") {
    val name = varchar("name", 200)
    val address = varchar("address", 200)
    val phone = varchar("phone", 50)

    val user = reference("user", UserTable, onDelete = ReferenceOption.CASCADE)
}

class ProfileEntity(uuid: EntityID<UUID>): UUIDEntity(uuid) {
    companion object : UUIDEntityClass<ProfileEntity>(ProfileTable)

    var name by ProfileTable.name
    var address by ProfileTable.address
    var phone by ProfileTable.phone
    var user by UserEntity referencedOn ProfileTable.user

    fun isEmpty(): Boolean {
        return name.isEmpty() && address.isEmpty() && phone.isEmpty()
    }
}
