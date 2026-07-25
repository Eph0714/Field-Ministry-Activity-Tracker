package com.fieldministry.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barangays")
data class BarangayEntity(
    @PrimaryKey val id: Int,
    val municipalityId: Int,
    val municipalityName: String?,
    val name: String,
)
