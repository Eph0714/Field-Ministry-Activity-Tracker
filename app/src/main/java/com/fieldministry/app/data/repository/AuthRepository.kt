package com.fieldministry.app.data.repository

import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.ChangePasswordRequest
import com.fieldministry.app.data.remote.dto.LoginRequest
import com.fieldministry.app.data.remote.dto.SignUpRequest
import com.fieldministry.app.data.session.SessionManager
import retrofit2.HttpException

sealed class LoginException(message: String) : Exception(message) {
    class AccountPending : LoginException("Account pending admin approval")
    class AccountRejected : LoginException("Account was rejected")
    class InvalidCredentials : LoginException("Invalid email or password")
}

class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager,
) {
    suspend fun signup(
        name: String,
        email: String,
        password: String,
        contactNumber: String?,
        phRegionId: Int?,
        phProvinceId: Int?,
        phMunicipalityId: Int?,
        phBarangayId: Int?,
    ) {
        api.signup(
            SignUpRequest(
                name = name,
                email = email,
                password = password,
                contactNumber = contactNumber,
                phRegionId = phRegionId,
                phProvinceId = phProvinceId,
                phMunicipalityId = phMunicipalityId,
                phBarangayId = phBarangayId,
            )
        )
    }

    suspend fun login(email: String, password: String) {
        try {
            val response = api.login(LoginRequest(email, password))
            sessionManager.save(response.token, response.user)
        } catch (e: HttpException) {
            val code = e.response()?.errorBody()?.string()?.let {
                Regex("\"code\"\\s*:\\s*\"(\\w+)\"").find(it)?.groupValues?.get(1)
            }
            throw when (code) {
                "ACCOUNT_PENDING" -> LoginException.AccountPending()
                "ACCOUNT_REJECTED" -> LoginException.AccountRejected()
                else -> LoginException.InvalidCredentials()
            }
        }
    }

    suspend fun refreshProfile() {
        val user = api.me()
        sessionManager.updateProfile(user)
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
    }

    fun logout() {
        sessionManager.clear()
    }
}
