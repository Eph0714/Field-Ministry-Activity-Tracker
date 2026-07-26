package com.fieldministry.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PhRegionDto(
    val id: Int,
    @SerializedName("psgc_code") val psgcCode: String,
    val name: String,
    val code: String?,
)

data class PhProvinceDto(
    val id: Int,
    @SerializedName("region_id") val regionId: Int,
    @SerializedName("psgc_code") val psgcCode: String,
    val name: String,
)

data class PhMunicipalityDto(
    val id: Int,
    @SerializedName("province_id") val provinceId: Int,
    @SerializedName("psgc_code") val psgcCode: String,
    val name: String,
    val type: String,
)

data class PhBarangayDto(
    val id: Int,
    @SerializedName("municipality_id") val municipalityId: Int,
    @SerializedName("psgc_code") val psgcCode: String,
    val name: String,
)
