package com.fieldministry.app.data.repository

import com.fieldministry.app.data.remote.ApiService
import com.fieldministry.app.data.remote.dto.PhBarangayDto
import com.fieldministry.app.data.remote.dto.PhMunicipalityDto
import com.fieldministry.app.data.remote.dto.PhProvinceDto
import com.fieldministry.app.data.remote.dto.PhRegionDto

/**
 * Read-mostly national PH address reference data (regions/provinces/municipalities/barangays).
 * Deliberately NOT cached in Room like the congregation-territory municipalities/barangays are -
 * at ~42,000 barangays this is too large to usefully mirror offline, and registration/profile
 * editing already requires connectivity to submit anyway. Each call goes straight to the server,
 * scoped to just the parent the user picked (never the full national list at once).
 */
class PhAddressRepository(private val api: ApiService) {

    suspend fun getRegions(): List<PhRegionDto> = api.getPhRegions()

    suspend fun getProvinces(regionId: Int): List<PhProvinceDto> = api.getPhProvinces(regionId)

    suspend fun getMunicipalities(provinceId: Int, search: String? = null): List<PhMunicipalityDto> =
        api.getPhMunicipalities(provinceId, search)

    suspend fun getBarangays(municipalityId: Int, search: String? = null): List<PhBarangayDto> =
        api.getPhBarangays(municipalityId, search)

    suspend fun createRegion(psgcCode: String, name: String, code: String?) {
        api.createPhRegion(mapOf("psgc_code" to psgcCode, "name" to name, "code" to code))
    }

    suspend fun updateRegion(id: Int, name: String, code: String?) {
        api.updatePhRegion(id, mapOf("name" to name, "code" to code))
    }

    suspend fun deleteRegion(id: Int) {
        api.deletePhRegion(id)
    }

    suspend fun createProvince(regionId: Int, psgcCode: String, name: String) {
        api.createPhProvince(mapOf("region_id" to regionId, "psgc_code" to psgcCode, "name" to name))
    }

    suspend fun updateProvince(id: Int, name: String) {
        api.updatePhProvince(id, mapOf("name" to name))
    }

    suspend fun deleteProvince(id: Int) {
        api.deletePhProvince(id)
    }

    suspend fun createMunicipality(provinceId: Int, psgcCode: String, name: String, type: String) {
        api.createPhMunicipality(mapOf("province_id" to provinceId, "psgc_code" to psgcCode, "name" to name, "type" to type))
    }

    suspend fun updateMunicipality(id: Int, name: String, type: String) {
        api.updatePhMunicipality(id, mapOf("name" to name, "type" to type))
    }

    suspend fun deleteMunicipality(id: Int) {
        api.deletePhMunicipality(id)
    }

    suspend fun createBarangay(municipalityId: Int, psgcCode: String, name: String) {
        api.createPhBarangay(mapOf("municipality_id" to municipalityId, "psgc_code" to psgcCode, "name" to name))
    }

    suspend fun updateBarangay(id: Int, name: String) {
        api.updatePhBarangay(id, mapOf("name" to name))
    }

    suspend fun deleteBarangay(id: Int) {
        api.deletePhBarangay(id)
    }
}
