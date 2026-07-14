package com.family.scratchapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,           // internal storage path after copy+resize
    val originalName: String,
    val isActive: Boolean = true,   // included in game rotation
    val addedAt: Long = System.currentTimeMillis()
)
