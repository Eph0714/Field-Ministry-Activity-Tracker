package com.fieldministry.app.ui.returnvisit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.HouseholderEntity
import com.fieldministry.app.data.repository.ReturnVisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReturnVisitFormState(
    val householder: HouseholderEntity? = null,
    val outcomeNotes: String = "",
    val isPotentialRv: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class ReturnVisitViewModel(private val returnVisitRepository: ReturnVisitRepository) : ViewModel() {

    private val _form = MutableStateFlow(ReturnVisitFormState())
    val form: StateFlow<ReturnVisitFormState> = _form

    fun onHouseholderSelected(householder: HouseholderEntity) = _form.update {
        it.copy(householder = householder, isPotentialRv = householder.isPotentialRv, error = null)
    }
    fun onOutcomeNotesChange(v: String) = _form.update { it.copy(outcomeNotes = v) }
    fun onPotentialRvChange(v: Boolean) = _form.update { it.copy(isPotentialRv = v) }

    fun save() {
        val current = _form.value
        val householder = current.householder
        if (householder == null) {
            _form.update { it.copy(error = "Select a householder first") }
            return
        }

        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, error = null) }
            try {
                returnVisitRepository.createLocal(
                    householderUuid = householder.uuid,
                    outcomeNotes = current.outcomeNotes.ifBlank { null },
                    isPotentialRv = current.isPotentialRv,
                )
                _form.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _form.update { it.copy(isSaving = false, error = "Failed to save. Your entry was kept locally if possible.") }
            }
        }
    }
}
