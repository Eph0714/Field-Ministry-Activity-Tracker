package com.fieldministry.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val uuid: String,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("approval_status") val approvalStatus: String,
)

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val user: UserDto)

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("contact_number") val contactNumber: String?,
    @SerializedName("ph_region_id") val phRegionId: Int?,
    @SerializedName("ph_province_id") val phProvinceId: Int?,
    @SerializedName("ph_municipality_id") val phMunicipalityId: Int?,
    @SerializedName("ph_barangay_id") val phBarangayId: Int?,
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String,
)

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
)

data class UpdateUserRequest(
    val name: String?,
    val role: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    val password: String?,
)

data class ApiMessage(val message: String?, val error: String?, val code: String?)
