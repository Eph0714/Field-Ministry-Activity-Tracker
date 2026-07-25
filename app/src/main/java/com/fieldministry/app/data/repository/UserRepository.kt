package com.fieldministry.app.data.repository

import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.CreateUserRequest
import com.fieldministry.app.data.remote.dto.UpdateUserRequest
import com.fieldministry.app.data.remote.dto.UserDto

class UserRepository(private val api: ApiService) {
    suspend fun pendingSignups(): List<UserDto> = api.getPendingSignups()

    suspend fun approveSignup(id: Int): UserDto = api.approveSignup(id)

    suspend fun rejectSignup(id: Int) {
        api.rejectSignup(id)
    }

    suspend fun list(): List<UserDto> = api.getUsers()

    suspend fun create(name: String, email: String, password: String, role: String): UserDto =
        api.createUser(CreateUserRequest(name, email, password, role))

    suspend fun update(id: Int, name: String? = null, role: String? = null, isActive: Boolean? = null, password: String? = null): UserDto =
        api.updateUser(id, UpdateUserRequest(name, role, isActive, password))

    suspend fun delete(id: Int) {
        api.deleteUser(id)
    }
}
