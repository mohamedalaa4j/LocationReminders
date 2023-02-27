package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    // Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun shouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("There is an error")
        } else {
            try {
                Result.Success(ArrayList(reminders))
            } catch (e: Exception) {
                Result.Error(e.localizedMessage)
            }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError) {
            Result.Error("There is an error")
        } else {
            var reminder: ReminderDTO? = null
            for (item in reminders) {
                if (item.id == id) {
                    reminder = item
                }
            }
            try {
                if (reminder != null) {
                    Result.Success(reminder)
                } else {
                    Result.Error("Reminder not found!")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage)

            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}