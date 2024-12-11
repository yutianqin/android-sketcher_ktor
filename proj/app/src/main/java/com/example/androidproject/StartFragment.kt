package com.example.androidproject

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import java.lang.Integer.min

class StartFragment : Fragment() {
    private lateinit var drawingDataViewModel: DrawingDataViewModel
    private val drawingViewModel: DrawingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve display metrics
        val displayMetrics = resources.displayMetrics

        // Calculate approximate width and height
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Since using a 1:1 aspect ratio, get smallest dimension
        val minDimension = min(screenWidth, screenHeight)

        val navController = findNavController()
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)

        // Obtain an instance of DrawingRepository
        val app = requireActivity().application as DrawingApp
        val drawingRepository = app.DrawingRepository

        // Initialize the DrawingDataViewModel using the obtained DrawingRepository
        val viewModelFactory = DrawingInfoViewModelFactory(drawingRepository)
        drawingDataViewModel = ViewModelProvider(this, viewModelFactory).get(DrawingDataViewModel::class.java)

        composeView.setContent {
            DrawingList(drawingDataViewModel.allDrawingData, navController, drawingViewModel, minDimension, minDimension)
        }
    }
}

@Composable
fun DrawingList(
    drawingsLiveData: LiveData<List<DrawingData>>,
    navController: NavController,
    drawingViewModel: DrawingViewModel,
    width:Int,
    height: Int, ) {
    val drawings by drawingsLiveData.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxHeight() // Make sure the Column takes up the full height of its container
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f) // This will make LazyColumn take up all available space, leaving enough for the Button
        ) {
            items(drawings) { drawing ->
                DrawingItem(drawing) {
                    drawing.thumbnail?.let { thumbnail ->
                        val decodedBitmap = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.size)
                        val mutableBitmap = decodedBitmap.copy(Bitmap.Config.ARGB_8888, true)
                        drawingViewModel.drawingBitmap.postValue(mutableBitmap)
                        navController.navigate(R.id.action_startFragment_to_drawFragment)
                    }
                }
            }
        }

        Button(
            onClick = {
                val blankBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                drawingViewModel.drawingBitmap.postValue(blankBitmap)
                navController.navigate(R.id.action_startFragment_to_drawFragment)
            },
            modifier = Modifier.fillMaxWidth()
                .testTag("ButtonTag")
        ) {
            Text("Create New Drawing")
        }

        Button(
            onClick = {
                navController.navigate(R.id.action_startFragment_to_sharedImagesFragment)
            },
            modifier = Modifier.fillMaxWidth()
                .testTag("ButtonTag")
        ) {
            Text(text = "Import and Shared")
        }
    }
}

@Composable
fun DrawingItem(drawing: DrawingData, onItemClicked: (DrawingData) -> Unit) {
    // Container for the item
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClicked(drawing) }
            .testTag("DrawingItem:${drawing.drawingTitle}"), // test tag
        verticalAlignment = Alignment.CenterVertically
    ) {
        drawing.thumbnail?.let { thumbnail ->
            Image(
                bitmap = thumbnail.toImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        // Text container
        Column {
            // Display the title
            Text(text = drawing.drawingTitle, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
            // Display the last modified date
            Text(text = drawing.lastModifiedDate.toString(), style = TextStyle(fontSize = 14.sp, color = Color.Gray))
        }
    }
}

private fun ByteArray.toImageBitmap(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    return bitmap.asImageBitmap()
}