package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.data.source.local.RemindersDao
import com.udacity.project4.data.source.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNull.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    //    TODO: Add testing implementation to the RemindersDao.kt
    private lateinit var reminderDao: RemindersDao
    private lateinit var dataBase: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        dataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        reminderDao = dataBase.reminderDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        dataBase.close()
    }

    @Test
    fun saveReminder_getReminderById() = runBlocking {
        //GIVEN saving a reminder
        val fakeReminder = ReminderDTO("reminder", "desc", "location", 100.00, 100.00, "1")
        dataBase.reminderDao().saveReminder(fakeReminder)
        //WHEN getting the reminders by this id
        val reminder = dataBase.reminderDao().getReminderById(fakeReminder.id)
        //THEN will return this reminder
        assertThat(reminder?.id, `is`(fakeReminder.id))
        assertThat(reminder?.title, `is`(fakeReminder.title))
        assertThat(reminder?.description, `is`(fakeReminder.description))
        assertThat(reminder?.latitude, `is`(fakeReminder.latitude))
        assertThat(reminder?.longitude, `is`(fakeReminder.longitude))
        assertThat(reminder?.location, `is`(fakeReminder.location))
    }

    @Test
    fun getReminderById_noReminderWithThisId_returnNull() {
        runBlocking {
            //GIVEN saving a reminder
            val fakeReminder = ReminderDTO("reminder", "desc", "location", 100.00, 100.00, "1")
            dataBase.reminderDao().saveReminder(fakeReminder)
            //WHEN getting the reminders by another id
            val reminder = dataBase.reminderDao().getReminderById("7")
            //THEN will return null
            assertThat(reminder, nullValue())
        }
    }
}