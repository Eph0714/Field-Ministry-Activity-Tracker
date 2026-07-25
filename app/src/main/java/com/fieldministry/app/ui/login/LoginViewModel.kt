package com.fieldministry.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.repository.AuthRepository
import com.fieldministry.app.data.repository.LoginException
import com.fieldministry.app.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    init {
        sessionManager.rememberedCredentials()?.let { (email, password) ->
            _state.update { it.copy(email = email, password = password, rememberMe = true) }
        }
    }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onRememberMeChange(value: Boolean) = _state.update { it.copy(rememberMe = value) }

    fun login() {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = "Email and password are required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.login(current.email, current.password)
                if (current.rememberMe) {
                    sessionManager.saveRememberedCredentials(current.email, current.password)
                } else {
                    sessionManager.clearRememberedCredentials()
                }
                _state.update { it.copy(isLoading = false, loggedIn = true) }
            } catch (e: LoginException) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Unable to reach the server. Check your connection.") }
            }
        }
    }
}
