package com.fieldministry.app.data.repository

import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.BibleStudySummaryRow
import com.fieldministry.app.data.remote.dto.HouseholderDto
import com.fieldministry.app.data.remote.dto.ReportsSummaryDto
import com.fieldministry.app.data.remote.dto.ReturnVisitSummaryRow
import com.fieldministry.app.data.remote.dto.SearchingSummaryRow

data class ReportFilters(
    val municipalityId: Int? = null,
    val barangayId: Int? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
)

class ReportRepository(private val api: ApiService) {

    suspend fun searchingSummary(filters: ReportFilters): List<SearchingSummaryRow> =
        api.searchingSummary(
            municipalityId = filters.municipalityId,
            barangayId = filters.barangayId,
            dateFrom = filters.dateFrom,
            dateTo = filters.dateTo,
        )

    suspend fun bibleStudySummary(filters: ReportFilters): List<BibleStudySummaryRow> =
        api.bibleStudySummary(
            municipalityId = filters.municipalityId,
            barangayId = filters.barangayId,
            dateFrom = filters.dateFrom,
            dateTo = filters.dateTo,
        )

    suspend fun returnVisitSummary(filters: ReportFilters): List<ReturnVisitSummaryRow> =
        api.returnVisitSummary(
            municipalityId = filters.municipalityId,
            barangayId = filters.barangayId,
            dateFrom = filters.dateFrom,
            dateTo = filters.dateTo,
        )

    suspend fun potentialReturnVisits(filters: ReportFilters): List<HouseholderDto> =
        api.potentialReturnVisits(municipalityId = filters.municipalityId, barangayId = filters.barangayId)

    suspend fun summary(filters: ReportFilters): ReportsSummaryDto =
        api.reportsSummary(
            municipalityId = filters.municipalityId,
            barangayId = filters.barangayId,
            dateFrom = filters.dateFrom,
            dateTo = filters.dateTo,
        )
}
