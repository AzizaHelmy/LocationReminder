package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import com.udacity.project4.ui.reminderlist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    //TODO: provide testing to the RemindersListViewModel and its live data objects

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var reminderListRepo: FakeDataSource
    private lateinit var context: Application
    private val fakeReminders =
        mutableListOf(
            ReminderDTO("myReminder1", "desc1", "location1", 100.00, 100.00, "1"),
            ReminderDTO("myReminder2", "desc2", "location2", 200.00, 200.00, "2"),
            ReminderDTO("myReminder3", "desc3", "location3", 300.00, 300.00, "3"),
            ReminderDTO("myReminder4", "desc4", "location4", 400.00, 400.00, "4"),
            ReminderDTO("myReminder5", "desc5", "location5", 500.00, 500.00, "5"),
        )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    fun init() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        reminderListRepo = FakeDataSource(fakeReminders)
        viewModel = RemindersListViewModel(context, reminderListRepo)
    }

    @Test
    fun loadReminder_LoadReminderFromDataSource() {
        viewModel.loadReminders()
        val expectedResult = fakeReminders.map {
            it.toReminderDataItem()
        }
        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(expectedResult))

    }
}