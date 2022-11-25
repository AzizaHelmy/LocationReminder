package com.udacity.project4.ui.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import java.util.*

class SelectLocationFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentSelectLocationBinding

    private val locationRequest =
        LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val locationPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                Toast.makeText(
                    requireContext(), "Location Permission granted", Toast.LENGTH_SHORT
                ).show()
                getMyCurrentLocation()
            } else Toast.makeText(
                requireContext(), "Location Permission Denied", Toast.LENGTH_SHORT
            ).show()
        }
    @SuppressLint("SuspiciousIndentation")
    private val locationSettingReqLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if(result.resultCode== Activity.RESULT_OK){
                Toast.makeText(
                    requireContext(), "Accept to open location", Toast.LENGTH_SHORT
                ).show()
                getMyCurrentLocation()
            }else
            Toast.makeText(
                requireContext(), "Denied to open location", Toast.LENGTH_SHORT
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) { // if true = user denied the request 2 times
            showAlertDialogForPermission()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectLocationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        setupMenu()
        enableMyLocation()
        binding.btnSaveLocation.setOnClickListener {
            findNavController().navigate(R.id.addReminderFragment)
        }
    }

    private fun showAlertDialogForPermission() {
        AlertDialog.Builder(requireContext()).apply {
            setIcon(R.drawable.attention)
            setTitle(getString(R.string.location_required_error))
            setMessage(getString(R.string.permission_denied_explanation))
            setPositiveButton("OK") { _, _ ->
                locationPermissionResult.launch(permissions)
            }
        }.show()
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        googleMap.apply {
            setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map))
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnMapClickListener {
                var snippets = String.format(
                    Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", it.latitude, it.longitude
                )
                moveCamera(CameraUpdateFactory.newLatLng(it))
                clear()
                addMarker(
                    MarkerOptions().position(it).title("Marker").snippet(snippets)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                )
                binding.btnSaveLocation.text = getString(R.string.save)
            }
            setOnPoiClickListener {
                clear()
                addMarker(MarkerOptions().position(it.latLng).title(it.name))?.showInfoWindow()
                it.latLng.latitude
                it.latLng.longitude
                it.name
                binding.btnSaveLocation.text = getString(R.string.save)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (isLocationEnabled()) {
                getMyCurrentLocation()
            } else {
                enableLocation()
            }
        } else {
            locationPermissionResult.launch(permissions)
        }
    }

    private fun enableLocation() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
           // getMyCurrentLocation()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    locationSettingReqLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun getMyCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val currentLocationRequest =
            CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        val currentLocation = fusedLocationClient.getCurrentLocation(currentLocationRequest, null)
        setMyLocationOnMap(currentLocation)
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationOnMap(currentLocation: Task<Location>) {
        currentLocation.addOnCompleteListener {
            mMap.apply {
                isMyLocationEnabled = true
                val myLocation = LatLng(it.result.latitude, it.result.longitude)
                addGroundOverlay(
                    GroundOverlayOptions().position(myLocation, 100f)
                        .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
                )
                val snippets = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    myLocation.latitude,
                    myLocation.longitude
                )
                addMarker(
                    MarkerOptions().position(myLocation).title("Marker in Egypt").snippet(snippets)
                )
                animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18f))
            }

            it.let {
                val lat = it.result.latitude
                val lng = it.result.longitude
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
            }

        }
    }

    private fun isPermissionGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            context?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.normal_map -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    R.id.hybrid_map -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    R.id.satellite_map -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    R.id.terrain_map -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}