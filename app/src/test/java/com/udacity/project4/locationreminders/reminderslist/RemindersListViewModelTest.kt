package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utilities.MainCoroutineRule
import com.udacity.project4.locationreminders.utilities.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    // provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private var remindersList = mutableListOf<ReminderDTO>()
    private lateinit var fakeDataSource: FakeDataSource
//    private  var fakeDataSource = FakeDataSource(remindersList)

    @Before
    fun setupViewModelAndFakeDataSource() {

        // To stop injecting the real data source
        stopKoin()

        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 29.976480, 31.131302)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 29.976480, 31.131302)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 29.976480, 31.131302)
        val reminder4 = ReminderDTO("Title3", "Description3", "Location3", 29.976480, 31.131302)
        val reminder5 = ReminderDTO("Title3", "Description3", "Location3", 29.976480, 31.131302)

        remindersList.add(reminder1)
        remindersList.add(reminder2)
        remindersList.add(reminder3)
        remindersList.add(reminder4)
        remindersList.add(reminder5)
        fakeDataSource = FakeDataSource(remindersList)

        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

    }

    @Test
    fun loadReminders() {

        // GIVEN a ViewModel
        // initialised in @Before method

        // WHEN reminders are loaded
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        mainCoroutineRule.resumeDispatcher()

        // THEN

        // showLoading = false
        assertThat(remindersListViewModel.showLoading.value, `is`(false))
        // showNoData = false
        assertThat(remindersListViewModel.showNoData.value, `is`(false))

        // reminder = reminders that is in the fakeDataSource
        assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue().size,
            `is`(remindersList.size)
        )

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()[0].title, `is`("Title1"))
        assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue()[1].description,
            `is`("Description2")
        )
        assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue()[2].location,
            `is`("Location3")
        )
        assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue()[3].latitude,
            `is`(29.976480)
        )
        assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue()[4].longitude,
            `is`(31.131302)
        )

    }

    @Test
    fun showNoData() = runBlockingTest {

        // GIVEN a ViewModel
        // initialised in @Before method

        // WHEN there is no reminders
        mainCoroutineRule.pauseDispatcher()
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.invalidateShowNoData()
        mainCoroutineRule.resumeDispatcher()

        // THEN showNoData = true
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

}