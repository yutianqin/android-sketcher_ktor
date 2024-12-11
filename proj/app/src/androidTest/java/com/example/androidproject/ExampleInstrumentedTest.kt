package com.example.androidproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.not
import org.junit.After


import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Date

@RunWith(AndroidJUnit4::class)
class DrawFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    @Test
    fun clickColorPickerButton_shouldShowPopup() {
        // Start the Fragment
        launchFragmentInContainer<DrawFragment>()
        // Click on the button
        onView(withId(R.id.colorPickerButton)).perform(click())
        // Check that the buttons are present
        onView(withId(R.id.redButton)).check(matches(isDisplayed()))
        onView(withId(R.id.blackButton)).check(matches(isDisplayed()))
        onView(withId(R.id.yellowButton)).check(matches(isDisplayed()))
        onView(withId(R.id.blueButton)).check(matches(isDisplayed()))
        onView(withId(R.id.greenButton)).check(matches(isDisplayed()))
    }

    @Test
    fun clickBrushPickerButton_shouldShowPopup() {
        // Start the Fragment
        launchFragmentInContainer<DrawFragment>()
        // Click on the button
        onView(withId(R.id.brushPickerButton)).perform(click())
        // Check that the buttons are present
        onView(withId(R.id.circleBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.squareBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.normalBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.size1)).check(matches(isDisplayed()))
        onView(withId(R.id.size2)).check(matches(isDisplayed()))
        onView(withId(R.id.size3)).check(matches(isDisplayed()))

    }
}

@RunWith(AndroidJUnit4::class)
class StartFragmentTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private lateinit var viewModel: DrawingViewModel

    private val dummyData = listOf(
        DrawingData(Date(), Date(), "Test Drawing 1", null, null),
        DrawingData(Date(), Date(), "Test Drawing 2", null, null)
    )

    @Before
    fun setUp() {
        val localNavController =
            staticCompositionLocalOf<NavController> { error("No NavController provided") }
        viewModel = DrawingViewModel()

        val width = 100
        val height = 100
        composeTestRule.setContent {

            CompositionLocalProvider(localNavController provides navController) {
                val drawingsLiveData: LiveData<List<DrawingData>> = liveData {
                    emit(dummyData)
                }
                DrawingList(drawingsLiveData, navController, viewModel, width, height)
            }
        }
    }

    @Test
    fun testDummyDataVisibility() {
        dummyData.forEach { drawing ->
            composeTestRule.onNodeWithTag("DrawingItem:${drawing.drawingTitle}")
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()
        }
    }

    @Test
    fun testButtonVisibilityAndEnabled() {
        composeTestRule.onNodeWithTag("ButtonTag")
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }
}

@RunWith(AndroidJUnit4::class)
class DrawingDaoTest {

    private lateinit var drawingDao: DrawingDao
    private lateinit var db: DrawingDatabase


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DrawingDatabase::class.java).build()
        drawingDao = db.drawingInfoDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndLoad() = runBlocking {
        val drawingData = DrawingData(Date(), Date(), "Test Drawing", null, null)
        drawingDao.addDrawingData(drawingData)

        // Load the inserted data
        val loaded = drawingDao.getDrawingDataWithId(1).first()

        // Assert that the loaded data is not null
        assertNotNull(loaded)

        // Assert that the loaded data matches the inserted data
        assertEquals(drawingData.drawingTitle, loaded?.drawingTitle)
        assertEquals(drawingData.createdDate, loaded?.createdDate)
        assertEquals(drawingData.lastModifiedDate, loaded?.lastModifiedDate)
        assertEquals(drawingData.imagePath, loaded?.imagePath)
        assertArrayEquals(drawingData.thumbnail, loaded?.thumbnail)
    }

    @Test
    @Throws(Exception::class)
    fun testGetDrawings() = runBlocking {
        val drawingData1 = DrawingData(Date(), Date(), "Test Drawing 1", null, null)
        val drawingData2 = DrawingData(Date(), Date(), "Test Drawing 2", null, null)

        val drawingData3 = DrawingData(Date(), Date(), "Test Drawing 3", null, null)

        drawingDao.addDrawingData(drawingData1)
        drawingDao.addDrawingData(drawingData2)

        val allDrawings = drawingDao.getDrawings().first()
        assertNotNull(allDrawings)
        assertEquals(2, allDrawings.size)
        assertTrue(allDrawings.any{it.drawingTitle == "Test Drawing 1"})
        assertTrue(allDrawings.any{it.drawingTitle == "Test Drawing 2"})


        assertFalse(drawingData1.equals(drawingData2))
        assertTrue(drawingData2.equals(drawingData2))
        assertTrue(drawingData3.equals(drawingData3))

    }

    @Test
    @Throws(Exception::class)
    fun insertAndUpdateTitle() = runBlocking {
        // Insert initial data
        val drawingData = DrawingData(Date(), Date(), "Initial Title", null, null)
        drawingDao.addDrawingData(drawingData)

        // Update title of inserted data
        drawingDao.updateDrawingTitle("Updated Title", 1)

        // Load updated data
        val updated = drawingDao.getDrawingDataWithId(1).first()

        // Assert that the updated data is not null
        assertNotNull(updated)

        // Assert that the title was updated correctly
        assertEquals("Updated Title", updated?.drawingTitle)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndUpdateThumbnailAndLastModified() = runBlocking {
        // Insert initial data
        val initialTime = Date()
        val drawingData = DrawingData(initialTime, initialTime, "Test Drawing", null, null)
        drawingDao.addDrawingData(drawingData)

        // Update thumbnail and last modified date
        val newTime = Date()
        val newThumbnail = byteArrayOf(1, 2, 3)
        drawingDao.updateThumbnailAndLastModified(newThumbnail, newTime, 1)

        // Load updated data
        val updated = drawingDao.getDrawingDataWithId(1).first()

        // Assert that the updated data is not null
        assertNotNull(updated)

        // Assert that the thumbnail and last modified date were updated correctly
        assertEquals(newTime, updated?.lastModifiedDate)
        assertArrayEquals(newThumbnail, updated?.thumbnail)
    }

}

//@RunWith(AndroidJUnit4::class)
//class StartFragmentTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
//    private lateinit var viewModel: DrawingViewModel
//
//    @Before
//    fun setup() {
//        val localNavController =
//            staticCompositionLocalOf<NavController> { error("No NavController provided") }
//
//        // Run on UI Thread
//        runBlocking(Dispatchers.Main) {
//            navController.setGraph(R.navigation.nav_graph)
//            navController.setCurrentDestination(R.id.startFragment)
//        }
//
//        viewModel = DrawingViewModel()
//        // Creating some dummy DrawingData for testing
//        val testDrawingData = DrawingData(
//            lastModifiedDate = Date(),
//            createdDate = Date(),
//            drawingTitle = "Test Drawing",
//            imagePath = null,
//            thumbnail = null
//        )
//
//        val drawingsLiveData: LiveData<List<DrawingData>> = MutableLiveData(listOf(testDrawingData))
//
//        val width = 100
//        val height = 100
//
//        composeTestRule.setContent {
//            CompositionLocalProvider(localNavController provides navController) {
//                DrawingList(drawingsLiveData, navController, viewModel, width, height)
//            }
//        }
//    }
//
//    @Test
//    fun testButton_clickNavigatesToDrawingFragment() {
//        // Find the button by its text
//        composeTestRule.onNodeWithText("Create New Drawing").performClick()
//
//        // Validate navigation happens
//        onView(withId(R.id.colorPickerButton)).check(matches(isDisplayed()))
//        onView(withId(R.id.brushPickerButton)).check(matches(isDisplayed()))
//        onView(withId(R.id.saveButton)).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun testDrawingItem_clickNavigatesToDrawingFragment() {
//        // Find the drawing item by its test tag and perform a click action
//        composeTestRule.onNodeWithTag("DrawingItem:Test Drawing").performClick()
//
//        // Validate navigation happens
//        onView(withId(R.id.colorPickerButton)).check(matches(isDisplayed()))
//        onView(withId(R.id.brushPickerButton)).check(matches(isDisplayed()))
//    }
//}

// Testing using mockito. So far just setting up the mocks
//@RunWith(AndroidJUnit4::class)
//class StartFragmentTest {
//
//    @get:Rule
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    // Mock the ViewModels
//    private val mockDrawingDataViewModel = mock(DrawingDataViewModel::class.java)
//    private lateinit var navController: TestNavHostController
//    private lateinit var scenario: FragmentScenario<StartFragment>
//
//    private lateinit var mockDrawingViewModel: DrawingViewModel
//    private lateinit var mockBrushTypeLiveData: MutableLiveData<BrushPickerFragment.BrushType>
//    private lateinit var mockPenSizeLiveData: MutableLiveData<Float>
//    private lateinit var activeDrawingDataLiveData: MutableLiveData<DrawingData?>
//    private lateinit var allDrawingDataLiveData: MutableLiveData<List<DrawingData>>
//
//
//    @Before
//    fun setup() {
//        // Setup NavController
//        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
//        navController.setGraph(R.navigation.nav_graph)
//
//        // Configure the mocks
//        mockDrawingViewModel = Mockito.mock(DrawingViewModel::class.java)
//        mockBrushTypeLiveData = Mockito.spy(MutableLiveData())
//        `when`(mockDrawingViewModel.currentBrushType).thenReturn(mockBrushTypeLiveData)
//        mockBrushTypeLiveData.value = BrushPickerFragment.BrushType.NORMAL
//
//        mockPenSizeLiveData = Mockito.spy(MutableLiveData())
//        `when`(mockDrawingViewModel.currentPenSize).thenReturn(mockPenSizeLiveData)
//        mockPenSizeLiveData.value = 5f
//
//        activeDrawingDataLiveData = mock()
//        whenever(mockDrawingViewModel.activeDrawingData).thenReturn(activeDrawingDataLiveData)
//        activeDrawingDataLiveData.value = null
//
//        allDrawingDataLiveData = MutableLiveData()
//        whenever(mockDrawingDataViewModel.allDrawingData).thenReturn(allDrawingDataLiveData)
//        allDrawingDataLiveData.value = listOf()
//
//        whenever(mockDrawingDataViewModel.activeDrawingData).thenReturn(activeDrawingDataLiveData)
//        activeDrawingDataLiveData.value = null
//
//        whenever(mockDrawingViewModel.drawingBitmap).thenReturn(MutableLiveData())
//        whenever(mockDrawingViewModel.currentColor).thenReturn(MutableLiveData(Color.BLACK))
//    }
//
//    @Test
//    fun testNavigationToDrawFragmentOnItemClick() {
//    }
//
//    @Test
//    fun testNavigationToDrawFragmentOnButton() {
//    }
//}