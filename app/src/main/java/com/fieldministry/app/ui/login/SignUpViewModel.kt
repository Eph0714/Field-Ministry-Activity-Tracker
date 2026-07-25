package com.fieldministry.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false,
)

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun submit() {
        val current = _state.value
        if (current.name.isBlank() || current.email.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = "All fields are required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signup(current.name, current.email, current.password)
                _state.update { it.copy(isLoading = false, submitted = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Sign up failed. That email may already be registered.") }
            }
        }
    }
}
