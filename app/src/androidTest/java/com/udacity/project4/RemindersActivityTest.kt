package com.udacity.project4

import android.Manifest
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.data.repo.RemindersLocalRepository
import com.udacity.project4.data.source.ReminderDataSource
import com.udacity.project4.data.source.local.LocalDB
import com.udacity.project4.ui.MainActivity
import com.udacity.project4.ui.reminderlist.RemindersListViewModel
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest: AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test
//: AutoCloseKoinTest()
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var grantePermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }
    //  TODO: add End to End testing to the app
    @Test
    fun lunchMainActivity_showSnakeBarAndToast() {
        val scenario=ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.fab_add_reminder)).perform(click())
        onView(withId(R.id.tv_title)).check(matches(withText("Title reminder")))
        onView(withId(R.id.tv_description)).perform(typeText("I need to buy something"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.fab_save_reminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(appContext.getString(R.string.select_location))))
        onView(withId(R.id.tv_select_location)).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.btn_save_location)).perform(click())
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.fab_save_reminder)).perform(click())
        onView(withText(appContext.getString(R.string.reminder_saved))).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }
}
