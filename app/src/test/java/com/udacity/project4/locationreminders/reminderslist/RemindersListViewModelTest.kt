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
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
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
            ReminderDTO("reminder1", "desc1", "location1", 100.00, 100.00, "1"),
            ReminderDTO("reminder2", "desc2", "location2", 200.00, 200.00, "2"),
            ReminderDTO("reminder3", "desc3", "location3", 300.00, 300.00, "3"),
            ReminderDTO("reminder4", "desc4", "location4", 400.00, 400.00, "4"),
            ReminderDTO("reminder5", "desc5", "location5", 500.00, 500.00, "5"),
        )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initializeViewModel() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        reminderListRepo = FakeDataSource(fakeReminders)
        viewModel = RemindersListViewModel(context, reminderListRepo)
    }

    @Test
    fun loadReminders_loadRemindersFromDataSource() {
        //GIVEN
        viewModel.loadReminders()
        //WHEN
        val expectedResult = fakeReminders.map {
            it.toReminderDataItem()
        }
        //THEN
        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(expectedResult))

    }

    @Test
    fun loadReminders_returnError() {
        //GIVEN
        reminderListRepo.setReturnError(true)
        //WHEN
        viewModel.loadReminders()
        //THEN
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Error retrieving reminders"))

    }

    @Test
    fun loadReminders_checkLoading() {
        mainCoroutineRule.runBlockingTest {
            //GIVEN
            mainCoroutineRule.pauseDispatcher()
            //WHEN
            viewModel.loadReminders()
            //THEN
            assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

            mainCoroutineRule.resumeDispatcher()
            assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

        }
    }
}