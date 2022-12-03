package com.udacity.project4.ui.savereminder

/**
 * Created by Aziza Helmy on 11/18/2022.
 */
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.ui.reminderlist.ReminderDataItem
import com.udacity.project4.ui.savereminder.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.utils.isDeviceLocationEnabled
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by activityViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderDataItem: ReminderDataItem
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private val locationRequest =
        LocationRequest.Builder(10000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(100)
            .setMaxUpdates(1)
            .build()

    @RequiresApi(Build.VERSION_CODES.Q)
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)

        PendingIntent.getBroadcast(
            requireContext(), 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }
    private val locationPermissionResultRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true) {
                _viewModel.showToast.value = getString(R.string.location_permission_granted)
               startGeofence(reminderDataItem)
            } else
                _viewModel.showSnackBarInt.value = R.string.location_permission_denied
        }

    @SuppressLint("SuspiciousIndentation")
    private val locationSettingPermissionResultRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
//TODO
            } else
                _viewModel.showErrorMessage.value =
                    getString(R.string.deny_to_open_location)
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveReminderBinding.inflate(layoutInflater)
        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) { // if true = user denied the request 2 times
           // showAlertDialogForLocationPermission()
        }

        binding.tvSelectLocation.setOnClickListener {
            findNavController().navigate(R.id.selectLocationFragment)
        }
        binding.fabSaveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            reminderDataItem= ReminderDataItem( title,
                description,
                location,
                latitude,
                longitude)
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                requestPermissionsAndAddGeofence()
            } else {
                _viewModel.showSnackBarInt.value = R.string.no_data
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermissionsAndAddGeofence() {
        if (isForegroundAndBackgroundPermissionGranted()) {
            if (isDeviceLocationEnabled()) {
                startGeofence(reminderDataItem)
            } else {
                enableDeviceLocation()
            }
        } else {
            requestForegroundAndBackgroundPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isForegroundAndBackgroundPermissionGranted(): Boolean {
        val foregroundPermissionApproved = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundPermissionApproved = if (runningQOrLater) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return foregroundPermissionApproved && backgroundPermissionApproved
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestForegroundAndBackgroundPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }
        locationPermissionResultRequest.launch(permissions)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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

    private fun enableDeviceLocation() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            startGeofence(reminderDataItem)
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
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
    private fun startGeofence(reminderDataItem: ReminderDataItem) {
        //1- Build the Geofence Object
        val geofenceBuilder = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        //2- Build the geofence request
        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            .addGeofence(geofenceBuilder)
            .build()
        //3-Add the new geofence request with the new geofence
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)
        .run {
                addOnSuccessListener {
                _viewModel.saveReminder(reminderDataItem)
            }
            addOnFailureListener {
                _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        const val GEOFENCE_RADIUS_IN_METERS = 150f
    }
}