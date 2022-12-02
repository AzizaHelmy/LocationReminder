package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.data.repo.RemindersLocalRepository
import com.udacity.project4.data.source.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    //    TODO: Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var dataBase: RemindersDatabase
    private lateinit var reminderLocalRepo: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createRepository() {
        dataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).build()
        val reminderDao = dataBase.reminderDao()

        reminderLocalRepo = RemindersLocalRepository(reminderDao, Dispatchers.Unconfined)
    }

    @After
    fun closeDataBase() {
        dataBase.close()
    }

    @Test
    fun saveReminder_getReminderById() = runBlocking {
        //GIVEN saving a reminder
        val fakeReminder = ReminderDTO("reminder", "desc", "location", 100.00, 100.00, "1")
        reminderLocalRepo.saveReminder(fakeReminder)
        //WHEN getting the reminders by this id
        val reminder = reminderLocalRepo.getReminder(fakeReminder.id)
        //THEN will return this reminder
        assertThat(reminder, `is`(Result.Success(fakeReminder)))
    }

    @Test
    fun getReminderById_noReminderWithThisId_returnError() {
        runBlocking {
            //GIVEN saving a reminder
            val fakeReminder = ReminderDTO("reminder", "desc", "location", 100.00, 100.00, "1")
            reminderLocalRepo.saveReminder(fakeReminder)
            //WHEN getting the reminders by another id
            val reminder = reminderLocalRepo.getReminder("7")
            //THEN will return error
            assertThat(reminder, `is`(Result.Error("Reminder Not Found!")))
        }
    }
}