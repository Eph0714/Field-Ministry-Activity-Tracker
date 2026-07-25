package com.fieldministry.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "householders")
data class HouseholderEntity(
    @PrimaryKey val uuid: String,
    val serverId: Int? = null,
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoUrl: String? = null,
    val localPhotoPath: String? = null,
    val status: String = "Potential",
    val topic: String? = null,
    val remarks: String? = null,
    val municipalityId: Int? = null,
    val municipalityName: String? = null,
    val barangayId: Int? = null,
    val barangayName: String? = null,
    val isPotentialRv: Boolean = false,
    val updatedAt: String? = null,
    val isDirty: Boolean = false,
    val photoDirty: Boolean = false,
    val potentialRvDirty: Boolean = false,
    val isDeleted: Boolean = false,
)
