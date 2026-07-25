package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.BarangayEntity
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import com.fieldministry.app.data.repository.ReferenceDataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageBarangaysViewModel(private val referenceDataRepository: ReferenceDataRepository) : ViewModel() {

    val municipalities: StateFlow<List<MunicipalityEntity>> =
        referenceDataRepository.observeMunicipalities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val barangays: StateFlow<List<BarangayEntity>> =
        referenceDataRepository.observeBarangays()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { runCatching { referenceDataRepository.refreshFromServer() } }
    }

    fun add(municipalityId: Int, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { runCatching { referenceDataRepository.createBarangay(municipalityId, name) } }
    }

    fun update(id: Int, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { runCatching { referenceDataRepository.updateBarangay(id, name) } }
    }

    fun delete(id: Int) {
        viewModelScope.launch { runCatching { referenceDataRepository.deleteBarangay(id) } }
    }
}
