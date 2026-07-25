package com.fieldministry.app.ui.biblestudy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.local.entity.HouseholderEntity
import com.fieldministry.app.data.repository.BibleStudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BibleStudyFormState(
    val householder: HouseholderEntity? = null,
    val publication: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class BibleStudyViewModel(private val bibleStudyRepository: BibleStudyRepository) : ViewModel() {

    private val _form = MutableStateFlow(BibleStudyFormState())
    val form: StateFlow<BibleStudyFormState> = _form

    fun onHouseholderSelected(householder: HouseholderEntity) = _form.update { it.copy(householder = householder, error = null) }
    fun onPublicationChange(v: String) = _form.update { it.copy(publication = v) }

    fun save(startTimeIso: String?, endTimeIso: String?, durationSeconds: Int) {
        val current = _form.value
        val householder = current.householder
        if (householder == null) {
            _form.update { it.copy(error = "Select a householder first") }
            return
        }

        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, error = null) }
            try {
                bibleStudyRepository.createLocal(
                    householderUuid = householder.uuid,
                    publication = current.publication.ifBlank { null },
                    startTime = startTimeIso,
                    endTime = endTimeIso,
                    durationSeconds = durationSeconds,
                )
                _form.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _form.update { it.copy(isSaving = false, error = "Failed to save. Your entry was kept locally if possible.") }
            }
        }
    }
}
