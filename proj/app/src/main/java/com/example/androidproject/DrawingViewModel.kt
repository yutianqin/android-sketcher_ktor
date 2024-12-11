package com.example.androidproject

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json


@Serializable
data class Drawings(
    val drawingTitle: String,
    val imagePath: String?,
    val user_uid: String?,
    val isUser: Boolean
)

open class DrawingViewModel : ViewModel() {

    val client = HttpClient {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json()
        }
    }

    init {
        viewModelScope.launch {
            fetchImages()
        }
    }

    open val drawingBitmap: MutableLiveData<Bitmap> = MutableLiveData()

    val currentColor: MutableLiveData<Int> = MutableLiveData(Color.BLACK)

    var currentBrushType: MutableLiveData<BrushPickerFragment.BrushType> = MutableLiveData(BrushPickerFragment.BrushType.NORMAL)
    var currentPenSize: MutableLiveData<Float> = MutableLiveData(5f)
    lateinit var currentUser: String


    private var _allUsersImages = MutableStateFlow<List<Drawings>>(emptyList())
    val allUsersImages: StateFlow<List<Drawings>> = _allUsersImages

    private val _thisUsersImages = MutableStateFlow<List<Drawings>>(emptyList())
    val thisUsersImages: StateFlow<List<Drawings>> = _thisUsersImages


    fun changeColor(color: Int) {
        currentColor.value = color
    }

    fun changePenType(brushType: BrushPickerFragment.BrushType) {
        currentBrushType.value = brushType
    }

    fun changePenSize(size: Float) {
        currentPenSize.value = size
    }

    suspend fun fetchImages() {
        try {
            val allImages: HttpResponse = client.get("http://10.0.2.2:8080/drawings/shared") {
                val userUid = FirebaseAuth.getInstance().currentUser?.uid
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    if (userUid != null) {
                        append(HttpHeaders.Authorization, userUid)
                    }
                }
            }

            val allImagesData = allImages.bodyAsText()
            println(allImagesData)

            val json = Json { ignoreUnknownKeys = true }
            val drawingsList: List<Drawings> = json.decodeFromString(ListSerializer(Drawings.serializer()), allImagesData)

            val allUsersList = mutableListOf<Drawings>()
            val thisUsersList = mutableListOf<Drawings>()

            for (drawing in drawingsList) {
                allUsersList.add(drawing) // Always add to allUsersList
                if (drawing.user_uid == FirebaseAuth.getInstance().uid) {
                    thisUsersList.add(drawing) // Only add to thisUsersList if UID matches
                }
            }

            _allUsersImages.value = allUsersList
            _thisUsersImages.value = thisUsersList

            println("drawingList: $drawingsList")

        } catch (e: Exception) {
            Log.e("Debuging from fetch Image", "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun fetchImageAsByteArray(url: String): ByteArray {
        return try {
            val response: HttpResponse = client.get(url)
            response.readBytes()
        } catch (e: Exception) {
            Log.e("Debuging from fetchImageAsByteArray", "Error: ${e.message}")
            e.printStackTrace()
            byteArrayOf() // or throw an exception
        }
    }

    fun deleteDrawingFromState(drawing: Drawings) {
        _allUsersImages.value = _allUsersImages.value.filter { it.drawingTitle != drawing.drawingTitle }
        _thisUsersImages.value = _thisUsersImages.value.filter { it.drawingTitle != drawing.drawingTitle }
    }
}