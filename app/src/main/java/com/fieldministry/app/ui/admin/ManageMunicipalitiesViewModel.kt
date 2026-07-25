package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import com.fieldministry.app.data.repository.ReferenceDataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageMunicipalitiesViewModel(private val referenceDataRepository: ReferenceDataRepository) : ViewModel() {

    val municipalities: StateFlow<List<MunicipalityEntity>> =
        referenceDataRepository.observeMunicipalities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { runCatching { referenceDataRepository.refreshFromServer() } }
    }

    fun add(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { runCatching { referenceDataRepository.createMunicipality(name) } }
    }

    fun update(id: Int, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { runCatching { referenceDataRepository.updateMunicipality(id, name) } }
    }

    fun delete(id: Int) {
        viewModelScope.launch { runCatching { referenceDataRepository.deleteMunicipality(id) } }
    }
}
