package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import com.udacity.project4.ui.reminderlist.ReminderDataItem
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var reminderListRepo: FakeDataSource
    private lateinit var context: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initializeViewModel() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        reminderListRepo = FakeDataSource()
        viewModel = SaveReminderViewModel(context, reminderListRepo)
    }

    @Test
    fun createReminder_validateSaveReminder() {
        //GIVEN
        val fakeReminder = ReminderDataItem("reminder", "desc", "location", 100.00, 100.00, "1")
        //WHEN
        viewModel.saveReminder(fakeReminder)
        //THEN
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
//        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(context.getString(R.string.reminder_saved)))
//        assertThat(viewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }

    @Test
    fun saveReminder_checkLoading() {
        mainCoroutineRule.runBlockingTest {
            //GIVEN
            val fakeReminder = ReminderDataItem("reminder", "desc", "location", 100.00, 100.00, "1")
            mainCoroutineRule.pauseDispatcher()
            //WHEN
            viewModel.saveReminder(fakeReminder)
            //THEN
            assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

            mainCoroutineRule.resumeDispatcher()
            assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

        }
    }

    @Test
    fun validateEnteredData_validData_returnTrue() {
        //GIVEN
        val fakeReminder = ReminderDataItem("reminder", "desc", "location", 100.00, 100.00)
        //WHEN
        val validateDate = viewModel.validateEnteredData(fakeReminder)
        //THEN
        assertThat(validateDate, `is`(true))
    }

    @Test
    fun saveReminder_reminderSaved() {
        //GIVEN
        val fakeReminder = ReminderDataItem("reminder1", "desc1", "location1", 100.00, 100.00)
        //WHEN
        viewModel.saveReminder(fakeReminder)
        //THEN
        val savedReminder = reminderListRepo.reminderList.first()
        assertThat(savedReminder.id, `is`(fakeReminder.id))
        assertThat(savedReminder.title, `is`(fakeReminder.title))
        assertThat(savedReminder.description, `is`(fakeReminder.description))
        assertThat(savedReminder.latitude, `is`(fakeReminder.latitude))
        assertThat(savedReminder.longitude, `is`(fakeReminder.longitude))
        assertThat(savedReminder.location, `is`(fakeReminder.location))
    }
}