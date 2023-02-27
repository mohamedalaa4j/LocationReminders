package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utilities.MainCoroutineRule
import com.udacity.project4.locationreminders.utilities.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    // provide testing to the SaveReminderView and its live data objects
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private var remindersList = mutableListOf<ReminderDTO>()
    private lateinit var fakeDataSource: FakeDataSource


    @Before
    fun setupViewModelAndTheDataSource() {
        stopKoin()
        fakeDataSource = FakeDataSource(remindersList)
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun cleanUp() {
        remindersList.clear()
    }

    @Test
    fun saveValidReminder_showToastSuccessMessage() {

        // GIVEN a ViewModel
        // initialized in @Before method

        // WHEN saving a valid reminder
        val reminder1 =
            ReminderDataItem("Title1", "Description1", "Location1", 29.976480, 31.131302)
        saveReminderViewModel.validateAndSaveReminder(reminder1)

        // THEN a toast should show "Reminder Saved !"
        MatcherAssert.assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            Matchers.`is`("Reminder Saved !")
        )
    }
}