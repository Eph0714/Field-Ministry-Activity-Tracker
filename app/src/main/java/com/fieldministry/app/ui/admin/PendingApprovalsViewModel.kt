package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.remote.dto.UserDto
import com.fieldministry.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingApprovalsState(
    val pending: List<UserDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class PendingApprovalsViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(PendingApprovalsState())
    val state: StateFlow<PendingApprovalsState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val pending = userRepository.pendingSignups()
                _state.update { it.copy(pending = pending, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to load pending approvals") }
            }
        }
    }

    fun approve(id: Int) {
        viewModelScope.launch {
            runCatching { userRepository.approveSignup(id) }
            refresh()
        }
    }

    fun reject(id: Int) {
        viewModelScope.launch {
            runCatching { userRepository.rejectSignup(id) }
            refresh()
        }
    }
}
