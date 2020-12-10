package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase
    private lateinit var appContext: Application

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        appContext = ApplicationProvider.getApplicationContext()

        database = Room.inMemoryDatabaseBuilder(
            appContext,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 32.1, 32.1)
        localDataSource.saveReminder(reminder1)

        // WHEN  - reminder1 retrieved by ID
        val result = localDataSource.getReminder(reminder1.id)
        result as Result.Success

        // THEN - Same task is returned
        Assert.assertThat(result.data.id, `is`(reminder1.id))
        Assert.assertThat(result.data.title, `is`(reminder1.title))
        Assert.assertThat(result.data.description, `is`(reminder1.description))
        Assert.assertThat(result.data.latitude, `is`(reminder1.latitude))
        Assert.assertThat(result.data.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun shouldReturnError_saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 32.1, 32.1)
        localDataSource.saveReminder(reminder1)

        // WHEN  - reminder1 retrieved by ID
        val result = localDataSource.getReminder("1323")
        result as Result.Error

        // THEN - Same task is returned
        Assert.assertThat(result.message, `is`("Reminder not found!"))

    }

    @Test
    fun deleteAllReminders_dataReminderIsEmpty() = runBlocking {

        localDataSource.deleteAllReminders()
        val result = localDataSource.getReminders()

        result as Result.Success
        Assert.assertThat(result.data.isEmpty(), `is`(true))
    }
}