package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    private var pointOfInterest: PointOfInterest? = null
    private var marker: Marker? = null

    //region Request permissions API
    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                Toast.makeText(requireContext(), "Location Permissions Granted", Toast.LENGTH_SHORT)
                    .show()
                getLocation()

            } else if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true){
                Toast.makeText(
                    requireContext(),
                    "Precise location is highly recommended for full app features",
                    Toast.LENGTH_SHORT
                ).show()
                getLocation()

            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        showInstructionsDialog()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // zoom to the user location after taking his permission
        checkIfPermissionsGrantedOrNot()

//        TODO: call this function after the user confirms on the selected location
        binding.btnSave.setOnClickListener {
            if (pointOfInterest == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_location),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                onLocationSelected()
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //  Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        map.isMyLocationEnabled = true
        getLocation()

        // put a marker to location that the user selected
        setMapLongClick(map)
        setPoiClick(map)

        // add style to the map
        setMapStyle(map)
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            val geocoder =
                Geocoder(requireContext(), Locale.getDefault()).getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                )

            val address = geocoder?.get(0)?.getAddressLine(0)

            val poi = PointOfInterest(latLng, null, address)

            pointOfInterest = poi
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker?.showInfoWindow()
            pointOfInterest = poi
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("setMapStyle", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("setMapStyle", "Can't find style. Error: ", e)
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

            if (location != null) {
                userLatitude = location.latitude
                userLongitude = location.longitude
                Log.e("location",userLatitude.toString())

                //show the location on the map
                val locationObject = LatLng(userLatitude!!, userLongitude!!)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationObject, 15f))
            }
        }

    }

    private fun checkIfPermissionsGrantedOrNot() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Request permissions if not
            requestLocationPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        } else {
            // If permissions is granted check the GPS & request location
            if (isGPSEnabled()) {
                getLocation()
            } else {
                Toast.makeText(
                    context,
                    "Location is disabled please enable it from the settings",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun onLocationSelected() {
        //         When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.savePoi(pointOfInterest)
    }

    private fun showInstructionsDialog() {

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Choose a location on the map")
            .setCancelable(true)

            .setPositiveButton("OK") { _, _ ->
            }

        // create dialog box
        val alert = dialogBuilder.create()

        // set title for alert dialog box
        alert.setTitle("Tips")

        alert.show()
    }
}


