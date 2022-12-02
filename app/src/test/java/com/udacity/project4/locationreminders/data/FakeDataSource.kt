package com.udacity.project4.locationreminders.data

import com.udacity.project4.data.source.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminderList: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {
    //    TODO: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setReturnError(shouldReturnError: Boolean) {
        this.shouldReturnError = shouldReturnError
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("Error retrieving reminders")
        } else {
            Result.Success(reminderList)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (!shouldReturnError) {
            val reminder = reminderList.find {
                it.id == id
            }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found!")
            }

        } else {
            Result.Error("Error retrieving reminder")
        }
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }
}