package com.fieldministry.app.data.repository

import com.fieldministry.app.data.local.dao.HouseholderDao
import com.fieldministry.app.data.local.dao.SearchingDao
import com.fieldministry.app.data.local.entity.SearchingEntity
import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.SearchingSessionRequest
import com.fieldministry.app.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.util.UUID

class SearchingRepository(
    private val api: ApiService,
    private val dao: SearchingDao,
    private val householderDao: HouseholderDao,
    private val sessionManager: SessionManager,
) {
    fun observeAll(): Flow<List<SearchingEntity>> = dao.observeAll()

    fun observeForHouseholder(householderUuid: String): Flow<List<SearchingEntity>> =
        dao.observeForHouseholder(householderUuid)

    suspend fun createLocal(
        householderUuid: String,
        languageSpoken: String?,
        preferredLanguage: String?,
        maritalStatus: String?,
        age: Int?,
        contactNumber: String?,
        remarks: String?,
        startTime: String?,
        endTime: String?,
        durationSeconds: Int,
    ): SearchingEntity {
        val publisherId = sessionManager.session.value?.userId ?: -1
        val householder = householderDao.getByUuid(householderUuid)
        val entity = SearchingEntity(
            uuid = UUID.randomUUID().toString(),
            householderUuid = householderUuid,
            householderServerId = householder?.serverId,
            householderName = householder?.name,
            publisherId = publisherId,
            languageSpoken = languageSpoken,
            preferredLanguage = preferredLanguage,
            maritalStatus = maritalStatus,
            age = age,
            contactNumber = contactNumber,
            remarks = remarks,
            startTime = startTime,
            endTime = endTime,
            durationSeconds = durationSeconds,
            isDirty = true,
        )
        dao.upsert(entity)
        return entity
    }

    suspend fun pushDirty() {
        for (item in dao.getDirty()) {
            try {
                var householderServerId = item.householderServerId
                if (householderServerId == null) {
                    householderServerId = householderDao.getByUuid(item.householderUuid)?.serverId
                }
                if (householderServerId == null) {
                    // Parent householder hasn't synced yet; retry after the next householder push.
                    continue
                }

                val request = SearchingSessionRequest(
                    uuid = item.uuid,
                    householderId = householderServerId,
                    languageSpoken = item.languageSpoken,
                    preferredLanguage = item.preferredLanguage,
                    maritalStatus = item.maritalStatus,
                    age = item.age,
                    contactNumber = item.contactNumber,
                    remarks = item.remarks,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    durationSeconds = item.durationSeconds,
                )

                val dto = try {
                    if (item.serverId == null) api.createSearchingSession(request) else api.updateSearchingSession(item.serverId, request)
                } catch (e: HttpException) {
                    if (e.code() == 409) {
                        val existingRemote = api.getSearchingSessions(householderId = householderServerId).firstOrNull { it.uuid == item.uuid }
                        existingRemote ?: throw e
                    } else {
                        throw e
                    }
                }
                dao.markSynced(item.uuid, dto.id)
            } catch (e: Exception) {
                // Leave dirty for retry on next sync.
            }
        }
    }

    suspend fun refreshFromServer() {
        val dirtyUuids = dao.getDirty().map { it.uuid }.toSet()
        val remote = api.getSearchingSessions()
        val entities = remote
            .filter { it.uuid !in dirtyUuids }
            .map {
                val householder = householderDao.getByServerId(it.householderId)
                SearchingEntity(
                    uuid = it.uuid,
                    serverId = it.id,
                    householderUuid = householder?.uuid ?: "",
                    householderServerId = it.householderId,
                    householderName = it.householderName,
                    publisherId = it.publisherId,
                    publisherName = it.publisherName,
                    languageSpoken = it.languageSpoken,
                    preferredLanguage = it.preferredLanguage,
                    maritalStatus = it.maritalStatus,
                    age = it.age,
                    contactNumber = it.contactNumber,
                    remarks = it.remarks,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    durationSeconds = it.durationSeconds,
                    updatedAt = it.updatedAt,
                )
            }
            .filter { it.householderUuid.isNotEmpty() }
        for (entity in entities) {
            try {
                dao.upsert(entity)
            } catch (e: Exception) {
                // Skip a single malformed row rather than failing the whole sync.
            }
        }
        dao.clearSyncedExcept((entities.map { it.uuid } + dirtyUuids).distinct())
    }

    suspend fun dirtyCount(): Int = dao.getDirty().size
}
