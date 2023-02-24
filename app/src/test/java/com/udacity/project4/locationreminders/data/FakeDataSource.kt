package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    // Create a fake data source to act as a double to the real data source
    private var thereIsError = false

    fun setThereIsError(value: Boolean) {
        thereIsError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (thereIsError) {
            return Result.Error("There is an error")
        } else {
            reminders?.let { return Result.Success(ArrayList(it)) }
            return Result.Error("Reminders not found")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (thereIsError) {
            return Result.Error("There is an error")
        } else {
            reminders?.let { reminders ->
                for (item in reminders) {
                    if (item.id == id) {
                        return Result.Success(item)
                    }
                }
            }
            return Result.Error("Reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}