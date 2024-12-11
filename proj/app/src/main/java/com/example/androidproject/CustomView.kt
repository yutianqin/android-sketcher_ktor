package com.example.androidproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = currentBrushSize
        style = Paint.Style.STROKE
    }
    private var path = Path()
    private var drawingBitmap: Bitmap? = null
    var viewModel: DrawingViewModel? = null

    private var currentBrushType: BrushPickerFragment.BrushType = BrushPickerFragment.BrushType.NORMAL
    private var currentBrushSize: Float = 5f
    private var offset: Float = 15F

    var isGyroDrawing = false
    private var lastGyroPosition: PointF? = null

    private var sensitivity = 100f
    init {
        val sensorEventListener = createSensorEventListener()
    }

    fun setBitmap(bitmap: Bitmap?) {
        drawingBitmap = bitmap
        invalidate()
    }

    fun attachTouchListener() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    when (currentBrushType) {
                        BrushPickerFragment.BrushType.NORMAL -> {
                            path.moveTo(event.x, event.y)
                        }

                        BrushPickerFragment.BrushType.CIRCLE -> {
                            path.addCircle(event.x, event.y, offset / 2, Path.Direction.CW)
                        }

                        BrushPickerFragment.BrushType.SQUARE -> {
                            val left = event.x - offset / 2
                            val top = event.y - offset / 2
                            val right = event.x + offset / 2
                            val bottom = event.y + offset / 2
                            path.addRect(left, top, right, bottom, Path.Direction.CW)
                        }
                    }
                    invalidate()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    when (currentBrushType) {
                        BrushPickerFragment.BrushType.NORMAL -> {
                            path.lineTo(event.x, event.y)
                        }

                        BrushPickerFragment.BrushType.CIRCLE -> {
                            path.addCircle(event.x, event.y, offset / 2, Path.Direction.CW)
                        }

                        BrushPickerFragment.BrushType.SQUARE -> {
                            val left = event.x - offset / 2
                            val top = event.y - offset / 2
                            val right = event.x + offset / 2
                            val bottom = event.y + offset / 2
                            path.addRect(left, top, right, bottom, Path.Direction.CW)
                        }
                    }
                    updateBitmap()
                    invalidate()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    when (currentBrushType) {
                        BrushPickerFragment.BrushType.NORMAL -> {
                        }

                        BrushPickerFragment.BrushType.CIRCLE -> {
                            path.addCircle(event.x, event.y, offset / 2, Path.Direction.CW)
                        }

                        BrushPickerFragment.BrushType.SQUARE -> {
                            val left = event.x - offset / 2
                            val top = event.y - offset / 2
                            val right = event.x + offset / 2
                            val bottom = event.y + offset / 2
                            path.addRect(left, top, right, bottom, Path.Direction.CW)
                        }
                    }
                    updateBitmap() // Save the current path to the bitmap
                    path.reset() // Reset the path so it's ready for new drawings
                    viewModel?.drawingBitmap?.value = this.getDrawingBitmap() // Safely accessing viewModel
                    performClick() // Not actually used, just adding so I don't get warnings
                    true
                }
                else -> false
            }
        }
    }

    fun changeBrushType(brushType: BrushPickerFragment.BrushType) {
        currentBrushType = brushType
        invalidate()
    }

    fun changeBrushSize(size: Float) {
        currentBrushSize = size
        paint.strokeWidth = size
        invalidate()
    }

    // Not actually used, just adding so I don't get warnings
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun updateBitmap() {
        if (drawingBitmap == null) {
            drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        val tempCanvas = Canvas(drawingBitmap!!)
        tempCanvas.drawPath(path, paint)
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        drawingBitmap?.let {
            canvas?.drawBitmap(it, 0f, 0f, null)
        }

    }
    fun getDrawingBitmap(): Bitmap? {
        return drawingBitmap
    }

    fun changePaintColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun startGyroDrawing() {
        isGyroDrawing = true
        val initialPosition = PointF(width / 2f, height / 2f)
        lastGyroPosition = initialPosition
        path.moveTo(initialPosition.x, initialPosition.y)
    }

    fun stopGyroDrawing() {
        isGyroDrawing = false
        lastGyroPosition = null
    }

    fun handleGyroInput(x: Float, y: Float) {
        if (isGyroDrawing) {
            lastGyroPosition?.let { lastPos ->
                // Calculate the new position based on gyroscope input
                val newX = lastPos.x + x * sensitivity
                val newY = lastPos.y + y * sensitivity
                // Clamp the values to make sure they're within the canvas bounds
                val clampedX = newX.coerceIn(0f, width.toFloat())
                val clampedY = newY.coerceIn(0f, height.toFloat())
                // Draw line from the last position to the new position.
                when (currentBrushType) {
                    BrushPickerFragment.BrushType.NORMAL -> {
                        path.lineTo(clampedX, clampedY)
                    }

                    BrushPickerFragment.BrushType.CIRCLE -> {
                        path.addCircle(clampedX, clampedY, offset / 2, Path.Direction.CW)
                    }

                    BrushPickerFragment.BrushType.SQUARE -> {
                        val left = clampedX - offset / 2
                        val top = clampedY - offset / 2
                        val right = clampedX + offset / 2
                        val bottom = clampedY + offset / 2
                        path.addRect(left, top, right, bottom, Path.Direction.CW)
                    }
                }
                // Update the last position for the next input.
                lastGyroPosition = PointF(clampedX, clampedY)
                // Update the canvas with the path's new segment.
                updateBitmap()
                viewModel?.drawingBitmap?.value = this.getDrawingBitmap()
                invalidate()
                val newPath = Path()
                newPath.moveTo(clampedX, clampedY)
                path = newPath
                true
            }
        }
    }

    private fun createSensorEventListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val y = it.values[1]

                    handleGyroInput(x, y)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
    }
}