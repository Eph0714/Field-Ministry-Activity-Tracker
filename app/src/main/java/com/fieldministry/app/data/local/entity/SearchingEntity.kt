package com.fieldministry.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "searching_sessions")
data class SearchingEntity(
    @PrimaryKey val uuid: String,
    val serverId: Int? = null,
    val householderUuid: String,
    val householderServerId: Int? = null,
    val householderName: String? = null,
    val publisherId: Int,
    val publisherName: String? = null,
    val languageSpoken: String? = null,
    val preferredLanguage: String? = null,
    val maritalStatus: String? = null,
    val age: Int? = null,
    val contactNumber: String? = null,
    val remarks: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val durationSeconds: Int = 0,
    val updatedAt: String? = null,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false,
)
