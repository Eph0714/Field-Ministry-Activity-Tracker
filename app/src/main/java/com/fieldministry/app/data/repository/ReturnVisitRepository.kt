package com.fieldministry.app.data.repository

import com.fieldministry.app.data.local.dao.HouseholderDao
import com.fieldministry.app.data.local.dao.ReturnVisitDao
import com.fieldministry.app.data.local.entity.ReturnVisitEntity
import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.ReturnVisitRequest
import com.fieldministry.app.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.time.Instant
import java.util.UUID

class ReturnVisitRepository(
    private val api: ApiService,
    private val dao: ReturnVisitDao,
    private val householderDao: HouseholderDao,
    private val sessionManager: SessionManager,
) {
    fun observeAll(): Flow<List<ReturnVisitEntity>> = dao.observeAll()

    fun observeForHouseholder(householderUuid: String): Flow<List<ReturnVisitEntity>> =
        dao.observeForHouseholder(householderUuid)

    suspend fun createLocal(
        householderUuid: String,
        outcomeNotes: String?,
        isPotentialRv: Boolean,
    ): ReturnVisitEntity {
        val publisherId = sessionManager.session.value?.userId ?: -1
        val householder = householderDao.getByUuid(householderUuid)
        val entity = ReturnVisitEntity(
            uuid = UUID.randomUUID().toString(),
            householderUuid = householderUuid,
            householderServerId = householder?.serverId,
            householderName = householder?.name,
            publisherId = publisherId,
            visitDatetime = Instant.now().toString(),
            outcomeNotes = outcomeNotes,
            isDirty = true,
        )
        dao.upsert(entity)

        if (householder != null && householder.isPotentialRv != isPotentialRv) {
            householderDao.upsert(householder.copy(isPotentialRv = isPotentialRv, potentialRvDirty = true))
        }

        return entity
    }

    suspend fun pushDirty() {
        for (item in dao.getDirty()) {
            try {
                val householderServerId = item.householderServerId
                    ?: householderDao.getByUuid(item.householderUuid)?.serverId
                    ?: continue

                val request = ReturnVisitRequest(
                    uuid = item.uuid,
                    householderId = householderServerId,
                    visitDatetime = item.visitDatetime,
                    outcomeNotes = item.outcomeNotes,
                    isPotentialRv = null,
                )

                val dto = try {
                    if (item.serverId == null) api.createReturnVisit(request) else api.updateReturnVisit(item.serverId, request)
                } catch (e: HttpException) {
                    if (e.code() == 409) {
                        val existingRemote = api.getReturnVisits(householderId = householderServerId).firstOrNull { it.uuid == item.uuid }
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
        val remote = api.getReturnVisits()
        val entities = remote
            .filter { it.uuid !in dirtyUuids }
            .map {
                val householder = householderDao.getByServerId(it.householderId)
                ReturnVisitEntity(
                    uuid = it.uuid,
                    serverId = it.id,
                    householderUuid = householder?.uuid ?: "",
                    householderServerId = it.householderId,
                    householderName = it.householderName,
                    publisherId = it.publisherId,
                    publisherName = it.publisherName,
                    visitDatetime = it.visitDatetime,
                    outcomeNotes = it.outcomeNotes,
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
