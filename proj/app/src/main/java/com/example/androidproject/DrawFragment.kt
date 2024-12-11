package com.example.androidproject

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import androidx.lifecycle.Observer
import android.widget.ImageButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Sharing Image
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import io.ktor.utils.io.streams.asInput


class DrawFragment : Fragment(R.layout.fragment_draw) {
    private val drawingViewModel: DrawingViewModel by activityViewModels()
    private lateinit var application: DrawingApp
    private lateinit var drawingRepository: DrawingRepository
    private lateinit var customView : CustomView
    private var gyroFlowJob: Job? = null

    private lateinit var sensorManager : SensorManager
    private lateinit var gyroscope : Sensor
    private var gyroSensorListener: SensorEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        application = requireActivity().application as DrawingApp
        drawingRepository = application.DrawingRepository

        customView = view.findViewById<CustomView>(R.id.custom_view)

        // Set the viewModel for the customView
        customView.viewModel = drawingViewModel

        customView.attachTouchListener()

        // Observe changes in the drawing Bitmap
        drawingViewModel.drawingBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            customView.setBitmap(bitmap)
        })

        // Observe changes in the currentColor from ViewModel and update CustomView's paint color
        drawingViewModel.currentColor.observe(viewLifecycleOwner, Observer { color ->
            customView.changePaintColor(color)
        })

        drawingViewModel.currentBrushType.observe(viewLifecycleOwner, Observer {brushType ->
            customView.changeBrushType(brushType) ;
        })

        drawingViewModel.currentPenSize.observe(viewLifecycleOwner, Observer { brushSize->
            customView.changeBrushSize(brushSize) ;
        })

        drawingViewModel.drawingBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            bitmap?.let {
                customView.setBitmap(it)
            }
        })

        val colorPickerButton: Button = view.findViewById(R.id.colorPickerButton)
        colorPickerButton.setOnClickListener {
            showColorPickerPopup(it)
        }

        val brushPickerButton: Button = view.findViewById(R.id.brushPickerButton)
        brushPickerButton.setOnClickListener {
            showBrushPickerPopup(it)
        }

        val saveButton: Button = view.findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveDrawing()
        }

        val gyroDrawButton: Button = view.findViewById(R.id.gyroDrawButton)

        // Set up the gyroscope
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!

        gyroDrawButton.setOnClickListener {
            if (customView.isGyroDrawing) {
                customView.stopGyroDrawing()
                stopGyroSensor()
                gyroDrawButton.text = "Start Gyro Draw"
            } else {
                customView.startGyroDrawing()
                startGyroSensor(sensorManager, gyroscope, customView)
                gyroDrawButton.text = "Stop Gyro Draw"
            }
        }

        val shareButton: Button = view.findViewById(R.id.shareButton)
        shareButton.setOnClickListener{
            onClickShare(it)
        }
    }
    fun showColorPickerPopup(anchorView: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.fragment_color_picker, null)

        // Initialize the buttons and set their click listeners
        val redButton: Button = popupView.findViewById(R.id.redButton)
        val blueButton: Button = popupView.findViewById(R.id.blueButton)
        val greenButton: Button = popupView.findViewById(R.id.greenButton)
        val yellowButton: Button = popupView.findViewById(R.id.yellowButton)
        val blackButton: Button = popupView.findViewById(R.id.blackButton)

        redButton.setOnClickListener { drawingViewModel.changeColor(Color.RED) }
        blueButton.setOnClickListener { drawingViewModel.changeColor(Color.BLUE) }
        greenButton.setOnClickListener { drawingViewModel.changeColor(Color.GREEN) }
        yellowButton.setOnClickListener { drawingViewModel.changeColor(Color.YELLOW) }
        blackButton.setOnClickListener { drawingViewModel.changeColor(Color.BLACK) }

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Measure the content view to set width and height explicitly
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popupWindow.width = popupView.measuredWidth
        popupWindow.height = popupView.measuredHeight

        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true

        popupWindow.showAtLocation(anchorView, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
    }

    private fun showBrushPickerPopup(anchorView: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.fragment_brush_picker, null)

        val normalPenButton: ImageButton = popupView.findViewById(R.id.normalBrush)
        val circlePenButton: ImageButton = popupView.findViewById(R.id.circleBrush)
        val squarePenButton: ImageButton = popupView.findViewById(R.id.squareBrush)
        val sizeButton1: Button = popupView.findViewById(R.id.size1)
        val sizeButton2: Button = popupView.findViewById(R.id.size2)
        val sizeButton3: Button = popupView.findViewById(R.id.size3)

        normalPenButton.setOnClickListener {
            drawingViewModel.changePenType(BrushPickerFragment.BrushType.NORMAL)
        }

        circlePenButton.setOnClickListener {
            drawingViewModel.changePenType(BrushPickerFragment.BrushType.CIRCLE)
        }

        squarePenButton.setOnClickListener {
            drawingViewModel.changePenType(BrushPickerFragment.BrushType.SQUARE)
        }

        sizeButton1.setOnClickListener {
            drawingViewModel.changePenSize(5f)
        }

        sizeButton2.setOnClickListener {
            drawingViewModel.changePenSize(10f)
        }

        sizeButton3.setOnClickListener {
            drawingViewModel.changePenSize(15f)
        }

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popupWindow.width = popupView.measuredWidth
        popupWindow.height = popupView.measuredHeight

        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true

        popupWindow.showAtLocation(anchorView, Gravity.TOP or Gravity.LEFT, 0, 0)
    }

    override fun onPause() {
        super.onPause()
        // When fragment goes to the background, save the current drawing state to ViewModel
        val customView = view?.findViewById<CustomView>(R.id.custom_view)
        drawingViewModel.drawingBitmap.value = customView?.getDrawingBitmap()
        gyroFlowJob?.cancel()
        drawingViewModel.changePenSize(5f)
        drawingViewModel.changePenType(BrushPickerFragment.BrushType.NORMAL)
        drawingViewModel.changeColor(Color.BLACK)
    }

    override fun onResume() {
        super.onResume()
        // If gyro drawing was active when the fragment was paused restart it
        if (customView.isGyroDrawing) {
            startGyroSensor(sensorManager, gyroscope, customView)
        }
    }
    private fun saveDrawing() {
        val customView = view?.findViewById<CustomView>(R.id.custom_view)
        val bitmap = customView?.getDrawingBitmap()!!

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // Get current date and time
        val currentDate = Date()

        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

        // Format the current date and time to string
        val formattedDateString = formatter.format(currentDate)

        // Use formatted string as drawing title
        val drawingTitle = "Drawing_$formattedDateString"

        val drawingData = DrawingData(
            lastModifiedDate = currentDate,
            createdDate = currentDate,
            drawingTitle = drawingTitle,
            imagePath = null,
            thumbnail = byteArray
        )

        drawingRepository.addNewDrawingInfo(drawingData)

    }

    private fun onClickShare(anchorView: View) {
        val customView = view?.findViewById<CustomView>(R.id.custom_view)
        val bitmap = customView?.getDrawingBitmap()

        bitmap?.let {
            val choices = arrayOf("Share via other apps", "Send to server")
            AlertDialog.Builder(requireContext())
                .setTitle("Choose an action")
                .setItems(choices) { _, which ->
                    when (which) {
                        0 -> shareImageAndText(it) // Share via other apps
                        1 -> sendImageToServer(it) // Send to server
                    }
                }.show()
        }
    }

    private val client = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    @OptIn(InternalAPI::class)
    private fun sendImageToServer(bitmap: Bitmap) {

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // Get current date and time
        val currentDate = Date()

        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

        // Format the current date and time to string
        val formattedDateString = formatter.format(currentDate)

        // Use formatted string as drawing title
        val drawingTitle = "Drawing_$formattedDateString"

        val drawingData = DrawingData(
            lastModifiedDate = currentDate,
            createdDate = currentDate,
            drawingTitle = drawingTitle,
            imagePath = null,
            thumbnail = byteArray
        )
        stream.close()
        // Using Kotlin coroutines to make the network call on a background IO thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse = client.post("http://10.0.2.2:8080/drawings") {
                    contentType(io.ktor.http.ContentType.MultiPart.FormData)

                    body = MultiPartFormDataContent(formData {
                        FirebaseAuth.getInstance().uid?.let { append("userUID", it) }
                        append("drawingTitle", drawingData.drawingTitle)
                        appendInput(
                            key = "image",
                            headers = Headers.build {
                                append(HttpHeaders.ContentDisposition, """form-data; name="drawingTitle"; filename="${drawingData.drawingTitle}"""")
                            },
                            size = byteArray.size.toLong()
                        ) { byteArray.inputStream().asInput() }
                    })
                }
                if (response.status.isSuccess()) {
                    withContext(Dispatchers.Main) {
                        Log.d("Debug", "Request succeeded with status: ${response.status}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("Debug", "Request failed with status: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun shareImageAndText(bitmap: Bitmap) {
        val uri = getUriOfTheImage(bitmap);

        val intent: Intent? = Intent(Intent.ACTION_SEND)
        intent?.putExtra(Intent.EXTRA_STREAM,uri)
        intent?.putExtra(Intent.EXTRA_TEXT,"Image Text ")
        intent?.putExtra(Intent.EXTRA_SUBJECT,"Image Subject")
        intent?.type = "image/*"
        startActivity(Intent.createChooser(intent, "Share Via"))
    }

    private fun getUriOfTheImage(bitmap: Bitmap): Uri? {

        val cacheDir = context?.cacheDir
        val folder = File(cacheDir, "images")
        var uri: Uri? = null

        folder.mkdirs()
        val file = File(folder, "image.jpeg")

        val fileOutputStream: FileOutputStream? = FileOutputStream(file)
        if (fileOutputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            uri = FileProvider.getUriForFile(requireContext(), "package com.example.androidproject", file)
        }
        return uri
    }

    private fun startGyroSensor(sensorManager: SensorManager, gyroscope: Sensor, customView: CustomView) {
        val gyroFlow: Flow<FloatArray> = getGyroData(gyroscope, sensorManager)

        gyroFlowJob?.cancel()
        gyroFlowJob = viewLifecycleOwner.lifecycleScope.launch {
            gyroFlow.collect { gyroReading ->
                customView.handleGyroInput(gyroReading[0], gyroReading[1])
            }
        }
    }

    fun stopGyroSensor() {
        if (gyroSensorListener != null) {
            sensorManager.unregisterListener(gyroSensorListener)
        }
    }

    @ExperimentalCoroutinesApi
    fun getGyroData(sensor: Sensor, sensorManager: SensorManager): Flow<FloatArray> = callbackFlow {
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
                    trySend(event.values.clone()).isSuccess
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        val isSensorRegistered = sensorManager.registerListener(
            sensorListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        if (!isSensorRegistered) {
            close(IllegalStateException("Could not register sensor listener"))
            return@callbackFlow
        }
        awaitClose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}