package com.example.androidproject

import android.content.Context
import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.util.Date

class ExampleUnitTest {
    private lateinit var viewModel: DrawingViewModel

    @Before
    fun setUp() {
        viewModel = DrawingViewModel()
    }

    @Test
    fun testDefaultValues() {
        assertNull(viewModel.drawingBitmap.value)
        assertEquals(Color.BLACK, viewModel.currentColor.value)
        assertEquals(BrushPickerFragment.BrushType.NORMAL, viewModel.currentBrushType.value)
        assertEquals(5f, viewModel.currentPenSize.value)
    }

//    @Test
//    fun changeColor_updates_currentColor() {
//        val newColor = Color.RED
//
//        viewModel.changeColor(newColor)
//
//        assertEquals(newColor, viewModel.currentColor.value)
//    }
//
//    @Test
//    fun changePenType_updates_currentBrushType() {
//        val newBrushType = BrushPickerFragment.BrushType.CIRCLE
//
//        viewModel.changePenType(newBrushType)
//
//        assertEquals(newBrushType, viewModel.currentBrushType.value)
//    }
//
//    @Test
//    fun changePenSize_updates_currentPenSize() {
//        val newSize = 10f
//
//        viewModel.changePenSize(newSize)
//
//        assertEquals(newSize, viewModel.currentPenSize.value)
//    }
}