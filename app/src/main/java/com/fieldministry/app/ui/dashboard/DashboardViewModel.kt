package com.fieldministry.app.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.BuildConfig
import com.fieldministry.app.data.repository.BibleStudyRepository
import com.fieldministry.app.data.repository.HouseholderRepository
import com.fieldministry.app.data.repository.ReturnVisitRepository
import com.fieldministry.app.data.repository.SearchingRepository
import com.fieldministry.app.data.repository.UserRepository
import com.fieldministry.app.data.session.Session
import com.fieldministry.app.data.session.SessionManager
import com.fieldministry.app.data.sync.SyncManager
import com.fieldministry.app.util.AppUpdate
import com.fieldministry.app.util.NetworkMonitor
import com.fieldministry.app.util.UpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val session: Session? = null,
    val isOnline: Boolean = true,
    val pendingCount: Int = 0,
    val pendingApprovalCount: Int = 0,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val searchingCount: Int = 0,
    val bibleStudyCount: Int = 0,
    val returnVisitCount: Int = 0,
    val householderCount: Int = 0,
    val availableUpdate: AppUpdate? = null,
    val isDownloadingUpdate: Boolean = false,
)

class DashboardViewModel(
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor,
    private val userRepository: UserRepository,
    private val searchingRepository: SearchingRepository,
    private val bibleStudyRepository: BibleStudyRepository,
    private val returnVisitRepository: ReturnVisitRepository,
    private val householderRepository: HouseholderRepository,
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
        viewModelScope.launch {
            combine(
                searchingRepository.observeAll(),
                bibleStudyRepository.observeAll(),
                returnVisitRepository.observeAll(),
                householderRepository.observeAll(),
            ) { searching, bibleStudies, returnVisits, householders ->
                DashboardCounts(searching.size, bibleStudies.size, returnVisits.size, householders.size)
            }.collect { counts ->
                _state.update {
                    it.copy(
                        searchingCount = counts.searching,
                        bibleStudyCount = counts.bibleStudy,
                        returnVisitCount = counts.returnVisit,
                        householderCount = counts.householder,
                    )
                }
            }
        }
        refreshPendingCount()
        refreshPendingApprovalCount()
        checkForUpdate()
    }

    private data class DashboardCounts(val searching: Int, val bibleStudy: Int, val returnVisit: Int, val householder: Int)

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

    private fun checkForUpdate() {
        viewModelScope.launch {
            val update = UpdateChecker.checkForUpdate(BuildConfig.BUILD_VERSION)
            _state.update { it.copy(availableUpdate = update) }
        }
    }

    fun installUpdate(context: Context) {
        val update = _state.value.availableUpdate ?: return
        viewModelScope.launch {
            _state.update { it.copy(isDownloadingUpdate = true) }
            try {
                UpdateChecker.downloadAndInstall(context, update)
            } catch (e: Exception) {
                // Leave availableUpdate set so the user can retry from the same banner.
            }
            _state.update { it.copy(isDownloadingUpdate = false) }
        }
    }

    fun dismissUpdate() {
        _state.update { it.copy(availableUpdate = null) }
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
