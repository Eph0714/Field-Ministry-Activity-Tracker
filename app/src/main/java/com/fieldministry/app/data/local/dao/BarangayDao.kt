package com.fieldministry.app.data.local.dao

import androidx.room.*
import com.fieldministry.app.data.local.entity.BarangayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarangayDao {
    @Query("SELECT * FROM barangays ORDER BY municipalityName ASC, name ASC")
    fun observeAll(): Flow<List<BarangayEntity>>

    @Query("SELECT * FROM barangays WHERE municipalityId = :municipalityId ORDER BY name ASC")
    fun observeForMunicipality(municipalityId: Int): Flow<List<BarangayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BarangayEntity>)

    @Query("DELETE FROM barangays")
    suspend fun clearAll()
}
