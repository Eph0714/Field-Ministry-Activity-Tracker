package com.fieldministry.app.data.local.dao

import androidx.room.*
import com.fieldministry.app.data.local.entity.ReturnVisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnVisitDao {
    @Query("SELECT * FROM return_visits WHERE isDeleted = 0 ORDER BY visitDatetime DESC")
    fun observeAll(): Flow<List<ReturnVisitEntity>>

    @Query("SELECT * FROM return_visits WHERE isDeleted = 0 AND householderUuid = :householderUuid ORDER BY visitDatetime DESC")
    fun observeForHouseholder(householderUuid: String): Flow<List<ReturnVisitEntity>>

    @Query("SELECT * FROM return_visits WHERE isDirty = 1")
    suspend fun getDirty(): List<ReturnVisitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ReturnVisitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ReturnVisitEntity>)

    @Query("UPDATE return_visits SET isDirty = 0, serverId = :serverId WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String, serverId: Int)

    @Query("DELETE FROM return_visits WHERE uuid = :uuid")
    suspend fun hardDelete(uuid: String)

    @Query("DELETE FROM return_visits WHERE isDirty = 0 AND uuid NOT IN (:keepUuids)")
    suspend fun clearSyncedExcept(keepUuids: List<String>)
}
