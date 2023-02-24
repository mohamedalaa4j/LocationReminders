package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
// Add testing implementation to the RemindersLocalRepository.kt

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun initRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries().build()

        remindersLocalRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminder_getReminderById() = runBlocking {

        // GIVEN - Insert a reminder.
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 29.976480, 31.131302)
        remindersLocalRepository.saveReminder(reminder1)

        // WHEN - Get the reminder by Id.
        val result = remindersLocalRepository.getReminder(reminder1.id)
                as com.udacity.project4.locationreminders.data.dto.Result.Success<ReminderDTO>

        val loaded = result.data

        // THEN - The loaded reminder is the same.
        assertThat(loaded.id, `is`(reminder1.id))
        assertThat(loaded.title, `is`(reminder1.title))
        assertThat(loaded.description, `is`(reminder1.description))
        assertThat(loaded.location, `is`(reminder1.location))
        assertThat(loaded.latitude, `is`(reminder1.latitude))
        assertThat(loaded.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun deleteAllReminders_getReminders() = runBlocking {

        // GIVEN - delete all reminders.
        remindersLocalRepository.deleteAllReminders()

        // WHEN - Get the reminders.
        val result = remindersLocalRepository.getReminders()
                as com.udacity.project4.locationreminders.data.dto.Result.Success<List<ReminderDTO>>

        val loadedList = result.data

        // THEN - The loaded reminders list is empty.
        assertThat(loadedList, `is`(emptyList()))
    }

    @Test
    fun nullReminder_getReminderById() = runBlocking {

        // GIVEN - Insert a reminder then delete.
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 29.976480, 31.131302)
        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.deleteAllReminders()

        // WHEN - Get the reminder.
        val result = remindersLocalRepository.getReminder(reminder1.id)
                as com.udacity.project4.locationreminders.data.dto.Result.Error

        // THEN - The loaded reminders list is empty.
        assertThat(result.message, `is`("Reminder not found!"))
    }

}