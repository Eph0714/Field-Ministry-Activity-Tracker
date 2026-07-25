package com.fieldministry.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HouseholderDto(
    val id: Int,
    val uuid: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("photo_url") val photoUrl: String?,
    val status: String,
    val topic: String?,
    val remarks: String?,
    @SerializedName("municipality_id") val municipalityId: Int?,
    @SerializedName("municipality_name") val municipalityName: String?,
    @SerializedName("barangay_id") val barangayId: Int?,
    @SerializedName("barangay_name") val barangayName: String?,
    @SerializedName("is_potential_rv") val isPotentialRv: Boolean,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class HouseholderRequest(
    val uuid: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val topic: String?,
    val remarks: String?,
    @SerializedName("municipality_id") val municipalityId: Int?,
    @SerializedName("barangay_id") val barangayId: Int?,
)

data class HouseholderHistoryDto(
    val searching: List<SearchingSessionDto>,
    val bibleStudies: List<BibleStudyDto>,
    val returnVisits: List<ReturnVisitDto>,
)
