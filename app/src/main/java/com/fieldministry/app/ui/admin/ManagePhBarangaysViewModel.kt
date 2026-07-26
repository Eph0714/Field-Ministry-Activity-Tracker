package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.remote.dto.PhBarangayDto
import com.fieldministry.app.data.remote.dto.PhMunicipalityDto
import com.fieldministry.app.data.remote.dto.PhProvinceDto
import com.fieldministry.app.data.remote.dto.PhRegionDto
import com.fieldministry.app.data.repository.PhAddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagePhBarangaysState(
    val regions: List<PhRegionDto> = emptyList(),
    val selectedRegion: PhRegionDto? = null,
    val provinces: List<PhProvinceDto> = emptyList(),
    val selectedProvince: PhProvinceDto? = null,
    val municipalities: List<PhMunicipalityDto> = emptyList(),
    val selectedMunicipality: PhMunicipalityDto? = null,
    val barangays: List<PhBarangayDto> = emptyList(),
    val search: String = "",
    val isLoading: Boolean = false,
)

class ManagePhBarangaysViewModel(private val repository: PhAddressRepository) : ViewModel() {

    private val _state = MutableStateFlow(ManagePhBarangaysState())
    val state: StateFlow<ManagePhBarangaysState> = _state

    init {
        viewModelScope.launch {
            val regions = runCatching { repository.getRegions() }.getOrDefault(emptyList())
            _state.update { it.copy(regions = regions) }
        }
    }

    fun onRegionSelected(region: PhRegionDto) {
        _state.update {
            it.copy(
                selectedRegion = region, provinces = emptyList(), selectedProvince = null,
                municipalities = emptyList(), selectedMunicipality = null, barangays = emptyList(),
                isLoading = true,
            )
        }
        viewModelScope.launch {
            val provinces = runCatching { repository.getProvinces(region.id) }.getOrDefault(emptyList())
            _state.update { it.copy(provinces = provinces, isLoading = false) }
        }
    }

    fun onProvinceSelected(province: PhProvinceDto) {
        _state.update {
            it.copy(selectedProvince = province, municipalities = emptyList(), selectedMunicipality = null, barangays = emptyList(), isLoading = true)
        }
        viewModelScope.launch {
            val municipalities = runCatching { repository.getMunicipalities(province.id) }.getOrDefault(emptyList())
            _state.update { it.copy(municipalities = municipalities, isLoading = false) }
        }
    }

    fun onMunicipalitySelected(municipality: PhMunicipalityDto) {
        _state.update { it.copy(selectedMunicipality = municipality, isLoading = true) }
        refresh()
    }

    fun onSearchChange(value: String) {
        _state.update { it.copy(search = value) }
        refresh()
    }

    fun refresh() {
        val municipalityId = _state.value.selectedMunicipality?.id ?: return
        val search = _state.value.search
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val barangays = runCatching { repository.getBarangays(municipalityId, search.ifBlank { null }) }.getOrDefault(emptyList())
            _state.update { it.copy(barangays = barangays, isLoading = false) }
        }
    }

    fun add(psgcCode: String, name: String) {
        val municipalityId = _state.value.selectedMunicipality?.id ?: return
        viewModelScope.launch {
            runCatching { repository.createBarangay(municipalityId, psgcCode, name) }
            refresh()
        }
    }

    fun update(id: Int, name: String) {
        viewModelScope.launch {
            runCatching { repository.updateBarangay(id, name) }
            refresh()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching { repository.deleteBarangay(id) }
            refresh()
        }
    }
}
