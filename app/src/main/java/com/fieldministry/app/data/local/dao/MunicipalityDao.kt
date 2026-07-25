package com.fieldministry.app.data.local.dao

import androidx.room.*
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MunicipalityDao {
    @Query("SELECT * FROM municipalities ORDER BY name ASC")
    fun observeAll(): Flow<List<MunicipalityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MunicipalityEntity>)

    @Query("DELETE FROM municipalities")
    suspend fun clearAll()
}
