package com.fieldministry.app.ui.householder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.HouseholderEntity
import com.fieldministry.app.data.remote.dto.SearchingSessionDto
import com.fieldministry.app.data.repository.HouseholderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HouseholderProfileState(
    val householder: HouseholderEntity? = null,
    val searchingHistory: List<SearchingSessionDto> = emptyList(),
    val isLoading: Boolean = true,
)

class HouseholderProfileViewModel(
    private val householderRepository: HouseholderRepository,
    private val uuid: String,
) : ViewModel() {

    private val _state = MutableStateFlow(HouseholderProfileState())
    val state: StateFlow<HouseholderProfileState> = _state

    init {
        viewModelScope.launch {
            val entity = householderRepository.getByUuid(uuid)
            _state.update { it.copy(householder = entity) }

            val serverId = entity?.serverId
            if (serverId != null) {
                val history = runCatching { householderRepository.history(serverId) }.getOrNull()
                _state.update { it.copy(searchingHistory = history?.searching ?: emptyList(), isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
