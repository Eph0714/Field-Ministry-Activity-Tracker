package com.fieldministry.app.ui.householder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.HouseholderEntity
import com.fieldministry.app.data.repository.HouseholderRepository
import com.fieldministry.app.data.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HouseholderListViewModel(
    private val householderRepository: HouseholderRepository,
    private val syncManager: SyncManager,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val householders: StateFlow<List<HouseholderEntity>> = _query
        .flatMapLatest { q ->
            if (q.isBlank()) householderRepository.observeAll() else householderRepository.observeSearch(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            runCatching { syncManager.pull() }
        }
    }

    fun onQueryChange(value: String) {
        _query.update { value }
    }
}
