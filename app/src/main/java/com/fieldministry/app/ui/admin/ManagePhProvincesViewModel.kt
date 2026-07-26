package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.remote.dto.PhProvinceDto
import com.fieldministry.app.data.remote.dto.PhRegionDto
import com.fieldministry.app.data.repository.PhAddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagePhProvincesState(
    val regions: List<PhRegionDto> = emptyList(),
    val selectedRegion: PhRegionDto? = null,
    val provinces: List<PhProvinceDto> = emptyList(),
    val isLoading: Boolean = false,
)

class ManagePhProvincesViewModel(private val repository: PhAddressRepository) : ViewModel() {

    private val _state = MutableStateFlow(ManagePhProvincesState())
    val state: StateFlow<ManagePhProvincesState> = _state

    init {
        viewModelScope.launch {
            val regions = runCatching { repository.getRegions() }.getOrDefault(emptyList())
            _state.update { it.copy(regions = regions) }
        }
    }

    fun onRegionSelected(region: PhRegionDto) {
        _state.update { it.copy(selectedRegion = region, isLoading = true) }
        refresh()
    }

    fun refresh() {
        val regionId = _state.value.selectedRegion?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val provinces = runCatching { repository.getProvinces(regionId) }.getOrDefault(emptyList())
            _state.update { it.copy(provinces = provinces, isLoading = false) }
        }
    }

    fun add(psgcCode: String, name: String) {
        val regionId = _state.value.selectedRegion?.id ?: return
        viewModelScope.launch {
            runCatching { repository.createProvince(regionId, psgcCode, name) }
            refresh()
        }
    }

    fun update(id: Int, name: String) {
        viewModelScope.launch {
            runCatching { repository.updateProvince(id, name) }
            refresh()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching { repository.deleteProvince(id) }
            refresh()
        }
    }
}
