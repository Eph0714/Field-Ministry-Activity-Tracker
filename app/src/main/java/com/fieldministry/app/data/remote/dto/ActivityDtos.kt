package com.fieldministry.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchingSessionDto(
    val id: Int,
    val uuid: String,
    @SerializedName("householder_id") val householderId: Int,
    @SerializedName("householder_name") val householderName: String?,
    @SerializedName("publisher_id") val publisherId: Int,
    @SerializedName("publisher_name") val publisherName: String?,
    @SerializedName("language_spoken") val languageSpoken: String?,
    @SerializedName("preferred_language") val preferredLanguage: String?,
    @SerializedName("marital_status") val maritalStatus: String?,
    val age: Int?,
    @SerializedName("contact_number") val contactNumber: String?,
    val remarks: String?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class SearchingSessionRequest(
    val uuid: String,
    @SerializedName("householder_id") val householderId: Int,
    @SerializedName("language_spoken") val languageSpoken: String?,
    @SerializedName("preferred_language") val preferredLanguage: String?,
    @SerializedName("marital_status") val maritalStatus: String?,
    val age: Int?,
    @SerializedName("contact_number") val contactNumber: String?,
    val remarks: String?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("duration_seconds") val durationSeconds: Int,
)

data class BibleStudyDto(
    val id: Int,
    val uuid: String,
    @SerializedName("householder_id") val householderId: Int,
    @SerializedName("householder_name") val householderName: String?,
    @SerializedName("publisher_id") val publisherId: Int,
    @SerializedName("publisher_name") val publisherName: String?,
    val publication: String?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class BibleStudyRequest(
    val uuid: String,
    @SerializedName("householder_id") val householderId: Int,
    val publication: String?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("duration_seconds") val durationSeconds: Int,
)

data class ReturnVisitDto(
    val id: Int,
    val uuid: String,
    @SerializedName("householder_id") val householderId: Int,
    @SerializedName("householder_name") val householderName: String?,
    @SerializedName("publisher_id") val publisherId: Int,
    @SerializedName("publisher_name") val publisherName: String?,
    @SerializedName("visit_datetime") val visitDatetime: String?,
    @SerializedName("outcome_notes") val outcomeNotes: String?,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class ReturnVisitRequest(
    val uuid: String,
    @SerializedName("householder_id") val householderId: Int,
    @SerializedName("visit_datetime") val visitDatetime: String?,
    @SerializedName("outcome_notes") val outcomeNotes: String?,
    @SerializedName("is_potential_rv") val isPotentialRv: Boolean?,
)
