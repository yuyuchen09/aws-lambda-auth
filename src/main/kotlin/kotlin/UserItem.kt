package kotlin

import com.google.gson.Gson
import lombok.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * Converted from java peer for Kotlin experiment.
 */
@Data
@With
@ToString
@NoArgsConstructor
@AllArgsConstructor
class UserItem {
    internal enum class Key {
        email, fullName, password
    }

    private val email: String? = null
    private val fullName: String? = null
    private val password: String? = null
    override fun equals(obj: Any?): Boolean {
        if (obj !is UserItem) {
            return false
        }
        val objItem = obj
        return email == objItem.email && fullName == objItem.fullName
    }

    fun toJson(): String {
        return GSON.toJson(this)
    }

    companion object {
        protected var GSON = Gson()
        fun fromJson(json: String?): UserItem {
            return GSON.fromJson(json, UserItem::class.java)
        }

        fun generateSecurePassword(password: String?): String {
            return BCryptPasswordEncoder().encode(password)
        }
    }
}