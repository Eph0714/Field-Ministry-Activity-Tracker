package com.fieldministry.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Postgres COUNT(*)/SUM(int) return as bigint, which node-pg (and therefore this JSON API)
// serializes as a numeric *string*, not a JSON number - hence String fields here.

data class SearchingSummaryRow(
    @SerializedName("publisher_id") val publisherId: Int,
    @SerializedName("publisher_name") val publisherName: String,
    @SerializedName("session_count") val sessionCount: String,
    @SerializedName("total_seconds") val totalSeconds: String,
)

data class BibleStudySummaryRow(
    @SerializedName("publisher_id") val publisherId: Int,
    @SerializedName("publisher_name") val publisherName: String,
    @SerializedName("study_count") val studyCount: String,
    @SerializedName("total_seconds") val totalSeconds: String,
)

data class ReturnVisitSummaryRow(
    @SerializedName("publisher_id") val publisherId: Int,
    @SerializedName("publisher_name") val publisherName: String,
    @SerializedName("visit_count") val visitCount: String,
)

data class SearchingTotals(
    @SerializedName("session_count") val sessionCount: String,
    @SerializedName("total_seconds") val totalSeconds: String,
)

data class BibleStudyTotals(
    @SerializedName("study_count") val studyCount: String,
    @SerializedName("total_seconds") val totalSeconds: String,
)

data class ReturnVisitTotals(
    @SerializedName("visit_count") val visitCount: String,
)

data class ReportsSummaryDto(
    val searching: SearchingTotals,
    val bibleStudies: BibleStudyTotals,
    val returnVisits: ReturnVisitTotals,
)
