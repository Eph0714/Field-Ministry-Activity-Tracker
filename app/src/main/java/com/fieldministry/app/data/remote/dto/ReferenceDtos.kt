package com.fieldministry.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MunicipalityDto(
    val id: Int,
    val name: String,
)

data class BarangayDto(
    val id: Int,
    @SerializedName("municipality_id") val municipalityId: Int,
    val name: String,
    @SerializedName("municipality_name") val municipalityName: String?,
)
