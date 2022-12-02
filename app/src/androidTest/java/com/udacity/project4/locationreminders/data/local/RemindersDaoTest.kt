package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.data.source.local.LocalDB
import com.udacity.project4.data.source.local.RemindersDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    //    TODO: Add testing implementation to the RemindersDao.kt
    private lateinit var reminderDao: RemindersDao
    private lateinit var db: LocalDB

    @Before
    fun createDb() {

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
       // db.close()
    }
}