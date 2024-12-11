package com.example.androidproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DrawingDao
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDrawingData(data: DrawingData)

    @Query("SELECT * from DrawingData ORDER BY lastModifiedDate DESC")
    fun getDrawings(): Flow<List<DrawingData>>


    @Query("SELECT * FROM DrawingData WHERE id = :id")
    fun getDrawingDataWithId(id: Int): Flow<DrawingData?>

    @Query("UPDATE DrawingData SET thumbnail = :thumbnailByteArray, lastModifiedDate = :lastModifiedTime WHERE id = :id")
    suspend fun updateThumbnailAndLastModified(
        thumbnailByteArray: ByteArray,
        lastModifiedTime: Date,
        id: Int
    )
    @Query("UPDATE DrawingData SET drawingTitle = :newTitle WHERE id = :id")
    suspend fun updateDrawingTitle(newTitle: String, id: Int)
}