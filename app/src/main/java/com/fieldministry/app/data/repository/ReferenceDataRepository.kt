package com.fieldministry.app.data.repository

import com.fieldministry.app.data.local.dao.BarangayDao
import com.fieldministry.app.data.local.dao.MunicipalityDao
import com.fieldministry.app.data.local.entity.BarangayEntity
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import com.fieldministry.app.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class ReferenceDataRepository(
    private val api: ApiService,
    private val municipalityDao: MunicipalityDao,
    private val barangayDao: BarangayDao,
) {
    fun observeMunicipalities(): Flow<List<MunicipalityEntity>> = municipalityDao.observeAll()

    fun observeBarangays(): Flow<List<BarangayEntity>> = barangayDao.observeAll()

    fun observeBarangaysForMunicipality(municipalityId: Int): Flow<List<BarangayEntity>> =
        barangayDao.observeForMunicipality(municipalityId)

    suspend fun refreshFromServer() {
        val municipalities = api.getMunicipalities()
        municipalityDao.upsertAll(municipalities.map { MunicipalityEntity(it.id, it.name) })

        val barangays = api.getBarangays()
        barangayDao.upsertAll(
            barangays.map { BarangayEntity(it.id, it.municipalityId, it.municipalityName, it.name) }
        )
    }

    suspend fun createMunicipality(name: String) {
        api.createMunicipality(mapOf("name" to name))
        refreshFromServer()
    }

    suspend fun updateMunicipality(id: Int, name: String) {
        api.updateMunicipality(id, mapOf("name" to name))
        refreshFromServer()
    }

    suspend fun deleteMunicipality(id: Int) {
        api.deleteMunicipality(id)
        refreshFromServer()
    }

    suspend fun createBarangay(municipalityId: Int, name: String) {
        api.createBarangay(mapOf("municipality_id" to municipalityId, "name" to name))
        refreshFromServer()
    }

    suspend fun updateBarangay(id: Int, name: String) {
        api.updateBarangay(id, mapOf("name" to name))
        refreshFromServer()
    }

    suspend fun deleteBarangay(id: Int) {
        api.deleteBarangay(id)
        refreshFromServer()
    }
}
