package com.fieldministry.app.data.repository

import com.fieldministry.app.data.local.dao.HouseholderDao
import com.fieldministry.app.data.local.entity.HouseholderEntity
import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.HouseholderHistoryDto
import com.fieldministry.app.data.remote.dto.HouseholderRequest
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.net.URLConnection
import java.util.UUID

class HouseholderRepository(
    private val api: ApiService,
    private val dao: HouseholderDao,
) {
    fun observeAll(): Flow<List<HouseholderEntity>> = dao.observeAll()

    fun observeSearch(query: String): Flow<List<HouseholderEntity>> = dao.observeSearch(query)

    suspend fun getByUuid(uuid: String): HouseholderEntity? = dao.getByUuid(uuid)

    suspend fun history(serverId: Int): HouseholderHistoryDto = api.getHouseholderHistory(serverId)

    suspend fun createLocal(
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        status: String,
        topic: String?,
        remarks: String?,
        municipalityId: Int?,
        municipalityName: String?,
        barangayId: Int?,
        barangayName: String?,
        localPhotoPath: String?,
    ): HouseholderEntity {
        val entity = HouseholderEntity(
            uuid = UUID.randomUUID().toString(),
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            localPhotoPath = localPhotoPath,
            status = status,
            topic = topic,
            remarks = remarks,
            municipalityId = municipalityId,
            municipalityName = municipalityName,
            barangayId = barangayId,
            barangayName = barangayName,
            isDirty = true,
            photoDirty = localPhotoPath != null,
        )
        dao.upsert(entity)
        return entity
    }

    suspend fun updateLocal(existing: HouseholderEntity, updated: HouseholderEntity) {
        dao.upsert(updated.copy(isDirty = true, photoDirty = existing.photoDirty))
    }

    suspend fun setLocalPhoto(uuid: String, localPhotoPath: String) {
        val existing = dao.getByUuid(uuid) ?: return
        dao.upsert(existing.copy(localPhotoPath = localPhotoPath, photoDirty = true, isDirty = true))
    }

    suspend fun setPotentialRvLocal(uuid: String, isPotentialRv: Boolean) {
        val existing = dao.getByUuid(uuid) ?: return
        dao.upsert(existing.copy(isPotentialRv = isPotentialRv, potentialRvDirty = true))
    }

    suspend fun deleteLocal(uuid: String) {
        val existing = dao.getByUuid(uuid) ?: return
        if (existing.serverId == null) {
            dao.hardDelete(uuid)
        } else {
            dao.upsert(existing.copy(isDeleted = true, isDirty = true))
        }
    }

    suspend fun pushDirty() {
        for (item in dao.getDirty()) {
            try {
                if (item.isDeleted && item.serverId != null) {
                    val response = api.deleteHouseholder(item.serverId)
                    if (response.isSuccessful) {
                        dao.hardDelete(item.uuid)
                    }
                    continue
                }

                var serverId = item.serverId
                if (item.isDirty) {
                    val request = HouseholderRequest(
                        uuid = item.uuid,
                        name = item.name,
                        address = item.address,
                        latitude = item.latitude,
                        longitude = item.longitude,
                        status = item.status,
                        topic = item.topic,
                        remarks = item.remarks,
                        municipalityId = item.municipalityId,
                        barangayId = item.barangayId,
                    )
                    val dto = try {
                        if (serverId == null) api.createHouseholder(request) else api.updateHouseholder(serverId, request)
                    } catch (e: HttpException) {
                        if (e.code() == 409) {
                            val existingRemote = api.getHouseholders(search = item.name).firstOrNull { it.uuid == item.uuid }
                            existingRemote ?: throw e
                        } else {
                            throw e
                        }
                    }
                    serverId = dto.id
                    dao.attachServerId(item.uuid, serverId)
                    dao.markClean(item.uuid)
                }

                if (item.photoDirty && item.localPhotoPath != null && serverId != null) {
                    val file = File(item.localPhotoPath)
                    if (file.exists()) {
                        val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "image/jpeg"
                        val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("photo", file.name, body)
                        val updatedDto = api.uploadHouseholderPhoto(serverId, part)
                        dao.markPhotoSynced(item.uuid, updatedDto.photoUrl ?: "")
                    } else {
                        dao.markPhotoSynced(item.uuid, item.photoUrl ?: "")
                    }
                }

                if (item.potentialRvDirty && serverId != null) {
                    api.setPotentialRv(serverId, mapOf("is_potential_rv" to item.isPotentialRv))
                    dao.markPotentialRvSynced(item.uuid)
                }
            } catch (e: Exception) {
                // Leave this record dirty; it will be retried on the next sync.
            }
        }
    }

    suspend fun refreshFromServer() {
        val dirtyUuids = dao.getDirty().map { it.uuid }.toSet()
        val remote = api.getHouseholders()
        val entities = remote
            .filter { it.uuid !in dirtyUuids }
            .map {
                HouseholderEntity(
                    uuid = it.uuid,
                    serverId = it.id,
                    name = it.name,
                    address = it.address,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    photoUrl = it.photoUrl,
                    status = it.status,
                    topic = it.topic,
                    remarks = it.remarks,
                    municipalityId = it.municipalityId,
                    municipalityName = it.municipalityName,
                    barangayId = it.barangayId,
                    barangayName = it.barangayName,
                    isPotentialRv = it.isPotentialRv,
                    updatedAt = it.updatedAt,
                )
            }
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
