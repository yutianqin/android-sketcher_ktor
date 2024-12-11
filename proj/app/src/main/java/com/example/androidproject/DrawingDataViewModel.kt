package com.example.androidproject

import androidx.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date

class DrawingDataViewModel(private val repository: DrawingRepository) : ViewModel()
{
    var activeDrawingData: MutableLiveData<DrawingData?> = MutableLiveData()

    val allDrawingData: LiveData<List<DrawingData>> = repository.allDrawingInfo

    private fun updateThumbnailForActiveDrawingInfo(thumbnail: ByteArray) {
        repository.updateDrawingInfoThumbnail(thumbnail, activeDrawingData.value?.id ?: 0)
    }
}

class DrawingInfoViewModelFactory(private val repository: DrawingRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawingDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawingDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}