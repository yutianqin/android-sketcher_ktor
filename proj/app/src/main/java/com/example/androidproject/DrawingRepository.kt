package com.example.androidproject

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow


class DrawingRepository(private val scope: CoroutineScope, private val dao: DrawingDao)
    {
        var activeDrawingData: MutableLiveData<DrawingData?> = MutableLiveData(null)
        val allDrawingInfo = dao.getDrawings().asLiveData()
        fun addNewDrawingInfo(drawingInfo: DrawingData) {
            scope.launch {
                dao.addDrawingData(drawingInfo)
            }
        }

        fun updateDrawingInfoThumbnail(bitmapToByteArray: ByteArray, id: Int) {
            scope.launch {
                dao.updateThumbnailAndLastModified(bitmapToByteArray, Date() ,id)
            }
        }

        suspend fun setActiveDrawingDataById(i: Int) {
            withContext(Dispatchers.IO) {
                Log.d("DrawingInfoRepository", "setActiveDrawingInfoById($i)")
                val newActiveDrawingInfo = dao.getDrawingDataWithId(i).firstOrNull()
                activeDrawingData.postValue(newActiveDrawingInfo)
                Log.d("DrawingInfoRepository", "activeDrawingInfo is set to ${activeDrawingData.value?.imagePath}")
            }
        }

        fun updateDrawingTitle(newTitle: String, id: Int) {
            scope.launch {
                dao.updateDrawingTitle(newTitle, id)
            }
        }

        fun getDrawingDataById(id: Int): Flow<DrawingData?> {
            return dao.getDrawingDataWithId(id)
        }
    }
