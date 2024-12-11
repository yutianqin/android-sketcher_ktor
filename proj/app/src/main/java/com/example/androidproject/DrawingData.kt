package com.example.androidproject

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.io.Serializable
import java.util.Date


//Room can not handle Date objects directly so we need type converters
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}

//Defines a SQLITE table
@Entity
data class DrawingData(
    var lastModifiedDate: Date,
    var createdDate: Date,
    var drawingTitle: String,
    var imagePath: String?, // Make imageUrl nullable
    var thumbnail: ByteArray?
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0 // integer primary key for the DB

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DrawingData

        if (lastModifiedDate != other.lastModifiedDate) {
            Log.d("Not Equals", "Last modified date is not equal")
            return false}
        if (createdDate != other.createdDate) {
            Log.d("Not Equals", "Date of Creation is not equal")
            return false}
        if (drawingTitle != other.drawingTitle){
            Log.d("Not Equals", "Drawing title is not equal")
            return false}
        if (imagePath != other.imagePath) {
            Log.d("Not Equals", "Image path is not equal")
            return false}
        if (thumbnail != null) {
            if (other.thumbnail == null) {
                Log.d("Not Equals", "Bitmap/Thumbnail is not equal")
                return false}
            if (!thumbnail.contentEquals(other.thumbnail)) {
                Log.d("Not Equals", "DContent of the images is not equal")
                return false}
        } else if (other.thumbnail != null) {
            Log.d("Is Null", "Thumbnail is null")
            return false}
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastModifiedDate.hashCode()
        result = 31 * result + createdDate.hashCode()
        result = 31 * result + drawingTitle.hashCode()
        result = 31 * result + (imagePath?.hashCode() ?: 0)
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + id
        return result
    }
}