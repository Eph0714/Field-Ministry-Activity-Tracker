package com.fieldministry.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.repository.UserRepository
import com.fieldministry.app.data.session.Session
import com.fieldministry.app.data.session.SessionManager
import com.fieldministry.app.data.sync.SyncManager
import com.fieldministry.app.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val session: Session? = null,
    val isOnline: Boolean = true,
    val pendingCount: Int = 0,
    val pendingApprovalCount: Int = 0,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
)

class DashboardViewModel(
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState(session = sessionManager.session.value, isOnline = networkMonitor.isOnline()))
    val state: StateFlow<DashboardState> = _state

    init {
        viewModelScope.launch {
            sessionManager.session.collect { session -> _state.update { it.copy(session = session) } }
        }
        viewModelScope.launch {
            networkMonitor.observe().collect { online -> _state.update { it.copy(isOnline = online) } }
        }
        refreshPendingCount()
        refreshPendingApprovalCount()
    }

    private fun refreshPendingCount() {
        viewModelScope.launch {
            val count = runCatching { syncManager.pendingCount() }.getOrDefault(0)
            _state.update { it.copy(pendingCount = count) }
        }
    }

    private fun refreshPendingApprovalCount() {
        if (!sessionManager.isAdmin()) return
        viewModelScope.launch {
            val count = runCatching { userRepository.pendingSignups().size }.getOrDefault(0)
            _state.update { it.copy(pendingApprovalCount = count) }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncError = null) }
            try {
                syncManager.sync()
                _state.update { it.copy(isSyncing = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isSyncing = false, syncError = "Sync failed. Try again when you have a connection.") }
            }
            refreshPendingCount()
        }
    }

    fun logout() {
        sessionManager.clear()
    }
}
