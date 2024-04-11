package hotkitchen.user

import hotkitchen.profile.ProfileEntity
import hotkitchen.profile.ProfileTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

@Serializable
enum class UserType(private val type: String) {
    @SerialName("staff")
    STAFF("staff"),
    @SerialName("client")
    CLIENT("client");

    override fun toString(): String {return this.type}

    companion object {
        private val mapping = values().associateBy(UserType::type)
        fun from(type: String) = mapping[type]
    }
}

@Serializable
data class NewUserDTO (val email: String, val userType: UserType, val password: String)
@Serializable
data class ExistingUserDTO (val email: String, val password: String)

object UserTable : UUIDTable("users") {
    val email = varchar("email", 80)
    val password = varchar("password", 50)
    val userType = enumerationByName("userType", 10, UserType::class)
}

class UserEntity(uuid: EntityID<UUID>): UUIDEntity(uuid) {
    companion object : UUIDEntityClass<UserEntity>(UserTable)

    var email by UserTable.email
    var userType by UserTable.userType
    var password by UserTable.password

    val profile by ProfileEntity referrersOn ProfileTable.user
}
