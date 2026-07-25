package com.fieldministry.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.BarangayEntity
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import com.fieldministry.app.data.remote.dto.BibleStudySummaryRow
import com.fieldministry.app.data.remote.dto.HouseholderDto
import com.fieldministry.app.data.remote.dto.ReportsSummaryDto
import com.fieldministry.app.data.remote.dto.ReturnVisitSummaryRow
import com.fieldministry.app.data.remote.dto.SearchingSummaryRow
import com.fieldministry.app.data.repository.ReferenceDataRepository
import com.fieldministry.app.data.repository.ReportFilters
import com.fieldministry.app.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportsState(
    val municipalityId: Int? = null,
    val barangayId: Int? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val summary: ReportsSummaryDto? = null,
    val searchingRows: List<SearchingSummaryRow> = emptyList(),
    val bibleStudyRows: List<BibleStudySummaryRow> = emptyList(),
    val returnVisitRows: List<ReturnVisitSummaryRow> = emptyList(),
    val potentialRv: List<HouseholderDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class ReportsViewModel(
    private val reportRepository: ReportRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state

    val municipalities: StateFlow<List<MunicipalityEntity>> =
        referenceDataRepository.observeMunicipalities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val barangays: StateFlow<List<BarangayEntity>> = _state
        .flatMapLatest { s ->
            s.municipalityId?.let { referenceDataRepository.observeBarangaysForMunicipality(it) }
                ?: referenceDataRepository.observeBarangays()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun onMunicipalityChange(id: Int?) {
        _state.update { it.copy(municipalityId = id, barangayId = null) }
        refresh()
    }

    fun onBarangayChange(id: Int?) {
        _state.update { it.copy(barangayId = id) }
        refresh()
    }

    fun onDateFromChange(value: String?) {
        _state.update { it.copy(dateFrom = value) }
        refresh()
    }

    fun onDateToChange(value: String?) {
        _state.update { it.copy(dateTo = value) }
        refresh()
    }

    fun refresh() {
        val current = _state.value
        val filters = ReportFilters(current.municipalityId, current.barangayId, current.dateFrom, current.dateTo)

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val summary = reportRepository.summary(filters)
                val searching = reportRepository.searchingSummary(filters)
                val bibleStudies = reportRepository.bibleStudySummary(filters)
                val returnVisits = reportRepository.returnVisitSummary(filters)
                val potentialRv = reportRepository.potentialReturnVisits(filters)
                _state.update {
                    it.copy(
                        summary = summary,
                        searchingRows = searching,
                        bibleStudyRows = bibleStudies,
                        returnVisitRows = returnVisits,
                        potentialRv = potentialRv,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to load reports") }
            }
        }
    }
}
