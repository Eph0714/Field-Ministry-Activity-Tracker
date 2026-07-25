package com.fieldministry.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_studies")
data class BibleStudyEntity(
    @PrimaryKey val uuid: String,
    val serverId: Int? = null,
    val householderUuid: String,
    val householderServerId: Int? = null,
    val householderName: String? = null,
    val publisherId: Int,
    val publisherName: String? = null,
    val publication: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val durationSeconds: Int = 0,
    val updatedAt: String? = null,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false,
)
