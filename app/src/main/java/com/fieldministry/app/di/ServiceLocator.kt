package com.fieldministry.app.di

import android.content.Context
import com.fieldministry.app.data.local.AppDatabase
import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.NetworkModule
import com.fieldministry.app.data.repository.AuthRepository
import com.fieldministry.app.data.repository.BibleStudyRepository
import com.fieldministry.app.data.repository.HouseholderRepository
import com.fieldministry.app.data.repository.PhAddressRepository
import com.fieldministry.app.data.repository.ReferenceDataRepository
import com.fieldministry.app.data.repository.ReportRepository
import com.fieldministry.app.data.repository.ReturnVisitRepository
import com.fieldministry.app.data.repository.SearchingRepository
import com.fieldministry.app.data.repository.UserRepository
import com.fieldministry.app.data.session.SessionManager
import com.fieldministry.app.data.sync.SyncManager
import com.fieldministry.app.util.NetworkMonitor

/**
 * Chosen over Hilt/Dagger to avoid annotation-processor version coupling; the object graph
 * here is small and static for the lifetime of the process.
 */
object ServiceLocator {
    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireContext(): Context =
        appContext ?: throw IllegalStateException("ServiceLocator.init() must be called before use")

    val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }

    val database: AppDatabase by lazy { AppDatabase.getInstance(requireContext()) }

    val api: ApiService by lazy { NetworkModule.create(sessionManager) }

    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(requireContext()) }

    val authRepository: AuthRepository by lazy { AuthRepository(api, sessionManager) }

    val referenceDataRepository: ReferenceDataRepository by lazy {
        ReferenceDataRepository(api, database.municipalityDao(), database.barangayDao())
    }

    val householderRepository: HouseholderRepository by lazy {
        HouseholderRepository(api, database.householderDao())
    }

    val searchingRepository: SearchingRepository by lazy {
        SearchingRepository(api, database.searchingDao(), database.householderDao(), sessionManager)
    }

    val bibleStudyRepository: BibleStudyRepository by lazy {
        BibleStudyRepository(api, database.bibleStudyDao(), database.householderDao(), sessionManager)
    }

    val returnVisitRepository: ReturnVisitRepository by lazy {
        ReturnVisitRepository(api, database.returnVisitDao(), database.householderDao(), sessionManager)
    }

    val userRepository: UserRepository by lazy { UserRepository(api) }

    val reportRepository: ReportRepository by lazy { ReportRepository(api) }

    val phAddressRepository: PhAddressRepository by lazy { PhAddressRepository(api) }

    val syncManager: SyncManager by lazy {
        SyncManager(
            referenceDataRepository,
            householderRepository,
            searchingRepository,
            bibleStudyRepository,
            returnVisitRepository,
            authRepository,
        )
    }
}
