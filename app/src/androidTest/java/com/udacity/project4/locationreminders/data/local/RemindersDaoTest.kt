package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Add testing implementation to the RemindersDao.kt

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminder_getReminderById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 29.976480, 31.131302)
        database.reminderDao().saveReminder(reminder1)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder1.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded?.id, `is`(reminder1.id))
        assertThat(loaded?.title, `is`(reminder1.title))
        assertThat(loaded?.description, `is`(reminder1.description))
        assertThat(loaded?.location, `is`(reminder1.location))
        assertThat(loaded?.latitude, `is`(reminder1.latitude))
        assertThat(loaded?.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun saveManyReminders_getReminders() = runBlockingTest {
        // GIVEN - Insert 3 reminders.
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 29.976480, 31.131302)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 29.976480, 31.131302)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 29.976480, 31.131302)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // WHEN - Get the all reminders from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains 3 objects.
        assertThat(loaded.size, `is`(3))
    }

    @Test
    fun saveManyReminders_deleteAllReminders() = runBlockingTest {
        // GIVEN - Insert 3 reminders.
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 29.976480, 31.131302)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 29.976480, 31.131302)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 29.976480, 31.131302)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // WHEN - Get the all reminders from the database after deleting all reminders.
        database.reminderDao().deleteAllReminders()
        val loaded = database.reminderDao().getReminders()


        // THEN - The loaded data contains no items.
        assertThat(loaded.size, `is`(0))
    }

}