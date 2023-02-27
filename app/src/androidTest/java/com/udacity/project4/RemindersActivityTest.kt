package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
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
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

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

    @After
    fun stopKoinAfterTest() {
        stopKoin()
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

    // add End to End testing to the app
    @Test
    fun launchTheApp_fragmentContainerIsDisplayed() {

        // GIVEN - RemindersActivity.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // monitor the fragment for idling resources
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // WHEN - RemindersActivity opens

        // THEN - nav_host_fragment is displayed
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun remindersActivityOpens_logoutIsDisplayed() {
        // GIVEN - RemindersActivity (After user login).
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // monitor the fragment for idling resources
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // WHEN - RemindersActivity opens

        // THEN - nav_host_fragment is displayed
        onView(withId(R.id.logout)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun saveReminderWithNoTitle_showSnackBar() {

        // GIVEN - SaveReminderFragment opened .
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // monitor the fragment for idling resources
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        // WHEN - Click save & the title is empty .
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - "Please enter title" snakeBar should show .
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }
}
