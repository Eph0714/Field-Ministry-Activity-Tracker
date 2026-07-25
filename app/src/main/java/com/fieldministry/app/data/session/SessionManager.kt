package com.fieldministry.app.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fieldministry.app.data.remote.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Session(
    val token: String,
    val userId: Int,
    val uuid: String,
    val name: String,
    val email: String,
    val role: String,
    val photoUrl: String?,
)

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "field_ministry_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _session = MutableStateFlow<Session?>(loadFromPrefs())
    val session: StateFlow<Session?> = _session

    private fun loadFromPrefs(): Session? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val userId = prefs.getInt(KEY_USER_ID, -1)
        if (userId == -1) return null
        return Session(
            token = token,
            userId = userId,
            uuid = prefs.getString(KEY_UUID, "") ?: "",
            name = prefs.getString(KEY_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            role = prefs.getString(KEY_ROLE, "publisher") ?: "publisher",
            photoUrl = prefs.getString(KEY_PHOTO_URL, null),
        )
    }

    fun save(token: String, user: UserDto) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_UUID, user.uuid)
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_ROLE, user.role)
            .putString(KEY_PHOTO_URL, user.photoUrl)
            .apply()
        _session.value = loadFromPrefs()
    }

    fun updateProfile(user: UserDto) {
        prefs.edit()
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_ROLE, user.role)
            .putString(KEY_PHOTO_URL, user.photoUrl)
            .apply()
        _session.value = loadFromPrefs()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_UUID)
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_ROLE)
            .remove(KEY_PHOTO_URL)
            .apply()
        _session.value = null
    }

    fun currentToken(): String? = _session.value?.token

    fun isLoggedIn(): Boolean = _session.value != null

    fun isOverseer(): Boolean = _session.value?.role.let { it == "overseer" || it == "admin" }

    fun isAdmin(): Boolean = _session.value?.role == "admin"

    fun saveRememberedCredentials(email: String, password: String) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, true)
            .putString(KEY_REMEMBERED_EMAIL, email)
            .putString(KEY_REMEMBERED_PASSWORD, password)
            .apply()
    }

    fun clearRememberedCredentials() {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, false)
            .remove(KEY_REMEMBERED_EMAIL)
            .remove(KEY_REMEMBERED_PASSWORD)
            .apply()
    }

    fun rememberedCredentials(): Pair<String, String>? {
        if (!prefs.getBoolean(KEY_REMEMBER_ME, false)) return null
        val email = prefs.getString(KEY_REMEMBERED_EMAIL, null) ?: return null
        val password = prefs.getString(KEY_REMEMBERED_PASSWORD, null) ?: return null
        return email to password
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_UUID = "uuid"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_PHOTO_URL = "photo_url"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_REMEMBERED_EMAIL = "remembered_email"
        private const val KEY_REMEMBERED_PASSWORD = "remembered_password"
    }
}
