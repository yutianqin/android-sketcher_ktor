package com.example.androidproject


import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import java.util.Date


class SharedImagesFragment : Fragment(R.layout.fragment_shared_images) {
    private val viewModel: DrawingViewModel by viewModels()
    private lateinit var application: DrawingApp
    private lateinit var drawingRepository: DrawingRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        application = requireActivity().application as DrawingApp
        drawingRepository = application.DrawingRepository


        val navController = findNavController()
        (view as? ComposeView)?.setContent {
            DrawingLists(navController, viewModel, drawingRepository)
        }
    }
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DrawingLists(
    navController: NavController,
    drawingViewModel: DrawingViewModel,
    drawingRepository: DrawingRepository
) {
    val allUsersImages by drawingViewModel.allUsersImages.collectAsState()
    val thisUsersImages by drawingViewModel.thisUsersImages.collectAsState()
    val baseURL = "http://10.0.2.2:8080/drawings/"
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Shared Images") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Navigate back to the startFragment
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text(
                        text = "All Shared Images",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                items(allUsersImages) { drawing ->
                    DrawingItems(drawing) {
                        scope.launch {
                            val imageUrl = "$baseURL/${drawing.drawingTitle}.png"
                            val imageBytes = drawingViewModel.fetchImageAsByteArray(imageUrl)

                            val drawingData = DrawingData(
                                lastModifiedDate = Date(),
                                createdDate = Date(),
                                drawingTitle = drawing.drawingTitle,
                                imagePath = null,
                                thumbnail = imageBytes
                            )

                            drawingRepository.addNewDrawingInfo(drawingData)
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text(
                        text = "Your Shared Images",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                items(thisUsersImages) { drawing ->
                    DrawingItems(drawing) {
                        scope.launch {
                            drawingViewModel.client.delete("http://10.0.2.2:8080/drawings/${drawing.drawingTitle}")
                            drawingViewModel.deleteDrawingFromState(drawing)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun DrawingItems(drawing: Drawings, onItemClicked: (Drawings) -> Unit)
{
    val baseURL = "http://10.0.2.2:8080/drawings/"
    val fullImagePath = "$baseURL/${drawing.drawingTitle}.png"

    // Container for the item
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClicked(drawing) }
            .testTag("DrawingItem:${drawing.drawingTitle}"), // test tag
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(data = fullImagePath),
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            // Display the title
            Text(
                text = drawing.drawingTitle,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}


private fun ByteArray.toImageBitmap(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    return bitmap.asImageBitmap()
}