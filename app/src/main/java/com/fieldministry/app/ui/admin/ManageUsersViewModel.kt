package com.fieldministry.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.remote.dto.UserDto
import com.fieldministry.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageUsersState(
    val users: List<UserDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class ManageUsersViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(ManageUsersState())
    val state: StateFlow<ManageUsersState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val users = userRepository.list()
                _state.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to load users") }
            }
        }
    }

    fun createUser(name: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            runCatching { userRepository.create(name, email, password, role) }
            refresh()
        }
    }

    fun updateUser(id: Int, name: String, role: String, isActive: Boolean) {
        viewModelScope.launch {
            runCatching { userRepository.update(id, name = name, role = role, isActive = isActive) }
            refresh()
        }
    }

    fun resetPassword(id: Int, newPassword: String) {
        viewModelScope.launch {
            runCatching { userRepository.update(id, password = newPassword) }
            refresh()
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            runCatching { userRepository.delete(id) }
            refresh()
        }
    }
}
