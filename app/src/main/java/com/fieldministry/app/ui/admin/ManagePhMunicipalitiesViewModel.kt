package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.remote.dto.PhMunicipalityDto
import com.fieldministry.app.data.remote.dto.PhProvinceDto
import com.fieldministry.app.data.remote.dto.PhRegionDto
import com.fieldministry.app.data.repository.PhAddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagePhMunicipalitiesState(
    val regions: List<PhRegionDto> = emptyList(),
    val selectedRegion: PhRegionDto? = null,
    val provinces: List<PhProvinceDto> = emptyList(),
    val selectedProvince: PhProvinceDto? = null,
    val municipalities: List<PhMunicipalityDto> = emptyList(),
    val isLoading: Boolean = false,
)

class ManagePhMunicipalitiesViewModel(private val repository: PhAddressRepository) : ViewModel() {

    private val _state = MutableStateFlow(ManagePhMunicipalitiesState())
    val state: StateFlow<ManagePhMunicipalitiesState> = _state

    init {
        viewModelScope.launch {
            val regions = runCatching { repository.getRegions() }.getOrDefault(emptyList())
            _state.update { it.copy(regions = regions) }
        }
    }

    fun onRegionSelected(region: PhRegionDto) {
        _state.update {
            it.copy(selectedRegion = region, provinces = emptyList(), selectedProvince = null, municipalities = emptyList(), isLoading = true)
        }
        viewModelScope.launch {
            val provinces = runCatching { repository.getProvinces(region.id) }.getOrDefault(emptyList())
            _state.update { it.copy(provinces = provinces, isLoading = false) }
        }
    }

    fun onProvinceSelected(province: PhProvinceDto) {
        _state.update { it.copy(selectedProvince = province, isLoading = true) }
        refresh()
    }

    fun refresh() {
        val provinceId = _state.value.selectedProvince?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val municipalities = runCatching { repository.getMunicipalities(provinceId) }.getOrDefault(emptyList())
            _state.update { it.copy(municipalities = municipalities, isLoading = false) }
        }
    }

    fun add(psgcCode: String, name: String, type: String) {
        val provinceId = _state.value.selectedProvince?.id ?: return
        viewModelScope.launch {
            runCatching { repository.createMunicipality(provinceId, psgcCode, name, type) }
            refresh()
        }
    }

    fun update(id: Int, name: String, type: String) {
        viewModelScope.launch {
            runCatching { repository.updateMunicipality(id, name, type) }
            refresh()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching { repository.deleteMunicipality(id) }
            refresh()
        }
    }
}
