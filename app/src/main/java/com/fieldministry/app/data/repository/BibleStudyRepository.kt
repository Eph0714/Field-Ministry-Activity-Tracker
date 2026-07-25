package com.fieldministry.app.data.repository

import com.fieldministry.app.data.local.dao.BibleStudyDao
import com.fieldministry.app.data.local.dao.HouseholderDao
import com.fieldministry.app.data.local.entity.BibleStudyEntity
import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.BibleStudyRequest
import com.fieldministry.app.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.util.UUID

class BibleStudyRepository(
    private val api: ApiService,
    private val dao: BibleStudyDao,
    private val householderDao: HouseholderDao,
    private val sessionManager: SessionManager,
) {
    fun observeAll(): Flow<List<BibleStudyEntity>> = dao.observeAll()

    fun observeForHouseholder(householderUuid: String): Flow<List<BibleStudyEntity>> =
        dao.observeForHouseholder(householderUuid)

    suspend fun createLocal(
        householderUuid: String,
        publication: String?,
        startTime: String?,
        endTime: String?,
        durationSeconds: Int,
    ): BibleStudyEntity {
        val publisherId = sessionManager.session.value?.userId ?: -1
        val householder = householderDao.getByUuid(householderUuid)
        val entity = BibleStudyEntity(
            uuid = UUID.randomUUID().toString(),
            householderUuid = householderUuid,
            householderServerId = householder?.serverId,
            householderName = householder?.name,
            publisherId = publisherId,
            publication = publication,
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
                val householderServerId = item.householderServerId
                    ?: householderDao.getByUuid(item.householderUuid)?.serverId
                    ?: continue

                val request = BibleStudyRequest(
                    uuid = item.uuid,
                    householderId = householderServerId,
                    publication = item.publication,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    durationSeconds = item.durationSeconds,
                )

                val dto = try {
                    if (item.serverId == null) api.createBibleStudy(request) else api.updateBibleStudy(item.serverId, request)
                } catch (e: HttpException) {
                    if (e.code() == 409) {
                        val existingRemote = api.getBibleStudies(householderId = householderServerId).firstOrNull { it.uuid == item.uuid }
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
        val remote = api.getBibleStudies()
        val entities = remote
            .filter { it.uuid !in dirtyUuids }
            .map {
                val householder = householderDao.getByServerId(it.householderId)
                BibleStudyEntity(
                    uuid = it.uuid,
                    serverId = it.id,
                    householderUuid = householder?.uuid ?: "",
                    householderServerId = it.householderId,
                    householderName = it.householderName,
                    publisherId = it.publisherId,
                    publisherName = it.publisherName,
                    publication = it.publication,
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
