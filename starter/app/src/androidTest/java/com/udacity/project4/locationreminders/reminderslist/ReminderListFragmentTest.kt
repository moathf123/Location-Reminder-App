package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

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

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun reminderList_saveReminder() = runBlocking {
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 32.1, 32.1)
        repository.saveReminder(reminder1)
        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.logout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderTitle)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map_view)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }


    @Test
    fun shouldReturnError_errorMessages() = runBlocking {
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 32.1, 32.1)
        repository.saveReminder(reminder1)
        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.logout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderTitle)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map_view)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.map_view)).perform(click())

        activityScenario.close()
    }

}


//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
