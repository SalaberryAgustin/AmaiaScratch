package com.family.scratchapp.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.family.scratchapp.data.models.Photo

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY addedAt DESC")
    fun getAllPhotos(): LiveData<List<Photo>>

    @Query("SELECT * FROM photos WHERE isActive = 1 ORDER BY addedAt ASC")
    suspend fun getActivePhotos(): List<Photo>

    @Insert
    suspend fun insert(photo: Photo): Long

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("UPDATE photos SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}
