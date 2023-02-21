package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT

                    PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT )

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            PendingIntent.getBroadcast(
//                requireContext(),
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//        }else {
//            PendingIntent.getBroadcast(
//                requireContext(),
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//        }

    }

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var remainderObject: ReminderDataItem

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private val requestLocationPermissions =
        if (runningQOrLater) {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true) {
                    addGeofence()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Location permissions all the time are required for the reminder feature",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        } else {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    addGeofence()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Location permissions are required for the reminder feature",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        binding.saveReminder.setOnClickListener {

            if (binding.reminderTitle.text.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.err_enter_title),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.reminderDescription.text.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.err_enter_description),
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                remainderObject = ReminderDataItem(
                    _viewModel.reminderTitle.value,
                    _viewModel.reminderDescription.value,
                    _viewModel.reminderSelectedLocationStr.value,
                    _viewModel.latitude.value,
                    _viewModel.longitude.value
                )

                if (!foregroundAndBackgroundLocationPermissionApproved()) {
                    requestForegroundAndBackgroundLocationPermissions()
                } else {
                    addGeofence()
                }


//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (runningQOrLater) {
            // Request permissions
            requestLocationPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        } else {
            // Request permissions
            requestLocationPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        if (_viewModel.validateAndSaveReminder(remainderObject)) {
            val geofence = Geofence.Builder()
                .setRequestId(remainderObject.id)
                .setCircularRegion(remainderObject.latitude!!, remainderObject.longitude!!, 200f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(), activity?.getString(R.string.geofences_added),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
//                    Toast.makeText(
//                        requireContext(), activity?.getString(R.string.geofences_not_added),
//                        Toast.LENGTH_SHORT
//                    ).show()

                }

            _viewModel.onClear()
        }
    }
}
