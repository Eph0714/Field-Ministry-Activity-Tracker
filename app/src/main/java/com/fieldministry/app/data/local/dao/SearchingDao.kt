package com.fieldministry.app.data.local.dao

import androidx.room.*
import com.fieldministry.app.data.local.entity.SearchingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchingDao {
    @Query("SELECT * FROM searching_sessions WHERE isDeleted = 0 ORDER BY startTime DESC")
    fun observeAll(): Flow<List<SearchingEntity>>

    @Query("SELECT * FROM searching_sessions WHERE isDeleted = 0 AND householderUuid = :householderUuid ORDER BY startTime DESC")
    fun observeForHouseholder(householderUuid: String): Flow<List<SearchingEntity>>

    @Query("SELECT * FROM searching_sessions WHERE isDirty = 1")
    suspend fun getDirty(): List<SearchingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SearchingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SearchingEntity>)

    @Query("UPDATE searching_sessions SET isDirty = 0, serverId = :serverId WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String, serverId: Int)

    @Query("DELETE FROM searching_sessions WHERE uuid = :uuid")
    suspend fun hardDelete(uuid: String)

    @Query("DELETE FROM searching_sessions WHERE isDirty = 0 AND uuid NOT IN (:keepUuids)")
    suspend fun clearSyncedExcept(keepUuids: List<String>)
}
