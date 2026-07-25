package com.fieldministry.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "return_visits")
data class ReturnVisitEntity(
    @PrimaryKey val uuid: String,
    val serverId: Int? = null,
    val householderUuid: String,
    val householderServerId: Int? = null,
    val householderName: String? = null,
    val publisherId: Int,
    val publisherName: String? = null,
    val visitDatetime: String? = null,
    val outcomeNotes: String? = null,
    val updatedAt: String? = null,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false,
)
