package com.fieldministry.app.ui.searching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.BarangayEntity
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import com.fieldministry.app.data.repository.HouseholderRepository
import com.fieldministry.app.data.repository.ReferenceDataRepository
import com.fieldministry.app.data.repository.SearchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchingFormState(
    val name: String = "",
    val address: String = "",
    val languageSpoken: String = "",
    val preferredLanguage: String = "",
    val maritalStatus: String = "",
    val age: String = "",
    val contactNumber: String = "",
    val remarks: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val localPhotoPath: String? = null,
    val municipalityId: Int? = null,
    val barangayId: Int? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class SearchingViewModel(
    private val householderRepository: HouseholderRepository,
    private val searchingRepository: SearchingRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(SearchingFormState())
    val form: StateFlow<SearchingFormState> = _form

    val municipalities: StateFlow<List<MunicipalityEntity>> =
        referenceDataRepository.observeMunicipalities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val barangays: StateFlow<List<BarangayEntity>> = _form
        .flatMapLatest { f ->
            f.municipalityId?.let { referenceDataRepository.observeBarangaysForMunicipality(it) }
                ?: referenceDataRepository.observeBarangays()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onNameChange(v: String) = _form.update { it.copy(name = v, error = null) }
    fun onAddressChange(v: String) = _form.update { it.copy(address = v) }
    fun onLanguageSpokenChange(v: String) = _form.update { it.copy(languageSpoken = v) }
    fun onPreferredLanguageChange(v: String) = _form.update { it.copy(preferredLanguage = v) }
    fun onMaritalStatusChange(v: String) = _form.update { it.copy(maritalStatus = v) }
    fun onAgeChange(v: String) = _form.update { it.copy(age = v) }
    fun onContactNumberChange(v: String) = _form.update { it.copy(contactNumber = v) }
    fun onRemarksChange(v: String) = _form.update { it.copy(remarks = v) }
    fun onMunicipalityChange(id: Int?) = _form.update { it.copy(municipalityId = id, barangayId = null) }
    fun onBarangayChange(id: Int?) = _form.update { it.copy(barangayId = id) }
    fun onLocationCaptured(lat: Double, lng: Double) = _form.update { it.copy(latitude = lat, longitude = lng) }
    fun onPhotoSelected(path: String) = _form.update { it.copy(localPhotoPath = path) }

    fun save(startTimeIso: String?, endTimeIso: String?, durationSeconds: Int) {
        val current = _form.value
        if (current.name.isBlank()) {
            _form.update { it.copy(error = "Householder name is required") }
            return
        }

        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, error = null) }
            try {
                val municipalityName = municipalities.value.firstOrNull { it.id == current.municipalityId }?.name
                val barangayName = barangays.value.firstOrNull { it.id == current.barangayId }?.name

                val householder = householderRepository.createLocal(
                    name = current.name,
                    address = current.address.ifBlank { null },
                    latitude = current.latitude,
                    longitude = current.longitude,
                    status = "Potential",
                    topic = null,
                    remarks = current.remarks.ifBlank { null },
                    municipalityId = current.municipalityId,
                    municipalityName = municipalityName,
                    barangayId = current.barangayId,
                    barangayName = barangayName,
                    localPhotoPath = current.localPhotoPath,
                )

                searchingRepository.createLocal(
                    householderUuid = householder.uuid,
                    languageSpoken = current.languageSpoken.ifBlank { null },
                    preferredLanguage = current.preferredLanguage.ifBlank { null },
                    maritalStatus = current.maritalStatus.ifBlank { null },
                    age = current.age.toIntOrNull(),
                    contactNumber = current.contactNumber.ifBlank { null },
                    remarks = current.remarks.ifBlank { null },
                    startTime = startTimeIso,
                    endTime = endTimeIso,
                    durationSeconds = durationSeconds,
                )

                _form.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _form.update { it.copy(isSaving = false, error = "Failed to save. Your entry was kept locally if possible.") }
            }
        }
    }
}
