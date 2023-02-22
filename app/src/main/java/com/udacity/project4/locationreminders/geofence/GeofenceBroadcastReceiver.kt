package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext


/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private var coroutineJob: Job = Job()
    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    override fun onReceive(context: Context, intent: Intent) {

// implement the onReceive method to receive the geofencing events at the background
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }

//    override fun onReceive(context: Context?, intent: Intent?) {
//        val geofencingEvent = GeofencingEvent.fromIntent(intent)
//        if (geofencingEvent.hasError()) {
//            val errorMessage = GeofenceStatusCodes
//                .getStatusCodeString(geofencingEvent.errorCode)
//            Log.e("Geofencing Event", errorMessage)
//            return
//        }
//
//        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
//            sendNotification(geofencingEvent.triggeringGeofences, context!!)
//        } else {
//            // Log the error.
//            Log.e("Geofencing Event", "Error")
//        }
//    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>, context: Context) {
        Log.e("geofence", "geofence")
        for (i in triggeringGeofences.indices) {
            val requestId = triggeringGeofences[i].requestId

            //Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    com.udacity.project4.utils.sendNotification(
                        context, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }


}