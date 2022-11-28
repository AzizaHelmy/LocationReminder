package com.udacity.project4.ui.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.isDeviceLocationEnabled
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
    private lateinit var binding: FragmentSelectLocationBinding
    override val _viewModel: SaveReminderViewModel by activityViewModel()
    private val locationRequest =
        LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val locationPermissionResultRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                _viewModel.showToast.value = getString(R.string.location_permission_granted)
                getMyCurrentLocation()
            } else
                _viewModel.showSnackBarInt.value = R.string.location_permission_denied

        }

    @SuppressLint("SuspiciousIndentation")
    private val locationSettingPermissionResultRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                getMyCurrentLocation()
            } else
                _viewModel.showErrorMessage.value =
                    getString(R.string.deny_to_open_location)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) { // if true = user denied the request 2 times
            showAlertDialogForLocationPermission()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectLocationBinding.inflate(layoutInflater)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        setupMenu()
        enableMyLocation()
        onLocationSelected()
    }

    private fun onLocationSelected() {
        binding.btnSaveLocation.setOnClickListener {
            if (marker == null) {
                _viewModel.showSnackBar.value =getString(R.string.err_select_location)
            } else {
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }

        }
    }

    private fun showAlertDialogForLocationPermission() {
        AlertDialog.Builder(requireContext()).apply {
            setIcon(R.drawable.attention)
            setTitle(getString(R.string.location_required_error))
            setMessage(getString(R.string.permission_denied_explanation))
            setPositiveButton("OK") { _, _ ->
                locationPermissionResultRequest.launch(permissions)
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
                marker = addMarker(
                    MarkerOptions().position(it).title( getString(R.string.dropped_pin)).snippet(snippets)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                )
                updateLocation(it, getString(R.string.dropped_pin))
               _viewModel.longitude.value = it.longitude
                binding.btnSaveLocation.text =
                    getString(R.string.save)
            }
            setOnPoiClickListener {
                clear()
                marker =
                    addMarker(MarkerOptions().position(it.latLng).title(it.name))
                marker?.showInfoWindow()
              updateLocation(it.latLng, it.name)
                binding.btnSaveLocation.text = getString(R.string.save)
            }
        }
    }

    private fun updateLocation(location: LatLng, locationName: String? = null) {
        _viewModel.latitude.value = location.latitude
        _viewModel.longitude.value = location.longitude
        _viewModel.reminderSelectedLocationStr.value = locationName
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (isDeviceLocationEnabled()) {
                getMyCurrentLocation()
            } else {
                enableDeviceLocation()
            }
        } else {
            locationPermissionResultRequest.launch(permissions)
        }
    }

    private fun enableDeviceLocation() {
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
                    locationSettingPermissionResultRequest.launch(
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

