package com.fieldministry.app.data.sync

import com.fieldministry.app.data.repository.AuthRepository
import com.fieldministry.app.data.repository.BibleStudyRepository
import com.fieldministry.app.data.repository.HouseholderRepository
import com.fieldministry.app.data.repository.ReferenceDataRepository
import com.fieldministry.app.data.repository.ReturnVisitRepository
import com.fieldministry.app.data.repository.SearchingRepository

/**
 * sync() pushes local changes then pulls — only ever called from a user-tapped Sync button.
 * pull() is read-only and safe to call automatically (e.g. on screen load).
 */
class SyncManager(
    private val referenceDataRepository: ReferenceDataRepository,
    private val householderRepository: HouseholderRepository,
    private val searchingRepository: SearchingRepository,
    private val bibleStudyRepository: BibleStudyRepository,
    private val returnVisitRepository: ReturnVisitRepository,
    private val authRepository: AuthRepository,
) {
    suspend fun sync() {
        // Households before their child activities, since activities need the parent's serverId.
        runCatching { householderRepository.pushDirty() }
        runCatching { searchingRepository.pushDirty() }
        runCatching { bibleStudyRepository.pushDirty() }
        runCatching { returnVisitRepository.pushDirty() }
        runCatching { referenceDataRepository.refreshFromServer() }
        runCatching { householderRepository.refreshFromServer() }
        runCatching { searchingRepository.refreshFromServer() }
        runCatching { bibleStudyRepository.refreshFromServer() }
        runCatching { returnVisitRepository.refreshFromServer() }
        runCatching { authRepository.refreshProfile() }
    }

    suspend fun pull() {
        runCatching { referenceDataRepository.refreshFromServer() }
        runCatching { householderRepository.refreshFromServer() }
        runCatching { searchingRepository.refreshFromServer() }
        runCatching { bibleStudyRepository.refreshFromServer() }
        runCatching { returnVisitRepository.refreshFromServer() }
    }

    suspend fun pendingCount(): Int {
        return householderRepository.dirtyCount() + searchingRepository.dirtyCount() +
            bibleStudyRepository.dirtyCount() + returnVisitRepository.dirtyCount()
    }
}
