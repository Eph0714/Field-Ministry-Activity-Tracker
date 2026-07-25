package com.fieldministry.app.data.local.dao

import androidx.room.*
import com.fieldministry.app.data.local.entity.HouseholderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholderDao {
    @Query("SELECT * FROM householders WHERE isDeleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<HouseholderEntity>>

    @Query(
        """SELECT * FROM householders
           WHERE isDeleted = 0 AND (name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%')
           ORDER BY name ASC"""
    )
    fun observeSearch(query: String): Flow<List<HouseholderEntity>>

    @Query("SELECT * FROM householders WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): HouseholderEntity?

    @Query("SELECT * FROM householders WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Int): HouseholderEntity?

    @Query("SELECT * FROM householders WHERE isDirty = 1 OR photoDirty = 1 OR potentialRvDirty = 1")
    suspend fun getDirty(): List<HouseholderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: HouseholderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<HouseholderEntity>)

    @Query("UPDATE householders SET isDirty = 0 WHERE uuid = :uuid")
    suspend fun markClean(uuid: String)

    @Query("UPDATE householders SET photoDirty = 0, localPhotoPath = NULL, photoUrl = :photoUrl WHERE uuid = :uuid")
    suspend fun markPhotoSynced(uuid: String, photoUrl: String)

    @Query("UPDATE householders SET potentialRvDirty = 0 WHERE uuid = :uuid")
    suspend fun markPotentialRvSynced(uuid: String)

    @Query("UPDATE householders SET serverId = :serverId WHERE uuid = :uuid")
    suspend fun attachServerId(uuid: String, serverId: Int)

    @Query("DELETE FROM householders WHERE uuid = :uuid")
    suspend fun hardDelete(uuid: String)

    @Query("DELETE FROM householders WHERE isDirty = 0 AND photoDirty = 0 AND uuid NOT IN (:keepUuids)")
    suspend fun clearSyncedExcept(keepUuids: List<String>)
}
