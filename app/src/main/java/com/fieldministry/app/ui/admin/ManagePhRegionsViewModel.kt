package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.remote.dto.PhRegionDto
import com.fieldministry.app.data.repository.PhAddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagePhRegionsState(val regions: List<PhRegionDto> = emptyList(), val isLoading: Boolean = true)

class ManagePhRegionsViewModel(private val repository: PhAddressRepository) : ViewModel() {

    private val _state = MutableStateFlow(ManagePhRegionsState())
    val state: StateFlow<ManagePhRegionsState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val regions = runCatching { repository.getRegions() }.getOrDefault(emptyList())
            _state.update { it.copy(regions = regions, isLoading = false) }
        }
    }

    fun add(psgcCode: String, name: String, code: String) {
        viewModelScope.launch {
            runCatching { repository.createRegion(psgcCode, name, code.ifBlank { null }) }
            refresh()
        }
    }

    fun update(id: Int, name: String, code: String) {
        viewModelScope.launch {
            runCatching { repository.updateRegion(id, name, code.ifBlank { null }) }
            refresh()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching { repository.deleteRegion(id) }
            refresh()
        }
    }
}
