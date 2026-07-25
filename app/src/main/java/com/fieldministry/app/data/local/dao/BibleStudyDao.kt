package com.fieldministry.app.data.local.dao

import androidx.room.*
import com.fieldministry.app.data.local.entity.BibleStudyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleStudyDao {
    @Query("SELECT * FROM bible_studies WHERE isDeleted = 0 ORDER BY startTime DESC")
    fun observeAll(): Flow<List<BibleStudyEntity>>

    @Query("SELECT * FROM bible_studies WHERE isDeleted = 0 AND householderUuid = :householderUuid ORDER BY startTime DESC")
    fun observeForHouseholder(householderUuid: String): Flow<List<BibleStudyEntity>>

    @Query("SELECT * FROM bible_studies WHERE isDirty = 1")
    suspend fun getDirty(): List<BibleStudyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BibleStudyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BibleStudyEntity>)

    @Query("UPDATE bible_studies SET isDirty = 0, serverId = :serverId WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String, serverId: Int)

    @Query("DELETE FROM bible_studies WHERE uuid = :uuid")
    suspend fun hardDelete(uuid: String)

    @Query("DELETE FROM bible_studies WHERE isDirty = 0 AND uuid NOT IN (:keepUuids)")
    suspend fun clearSyncedExcept(keepUuids: List<String>)
}
