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
import com.udacity.project4.ui.savereminder.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.utils.isDeviceLocationEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
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
                // getMyCurrentLocation()
            } else
                _viewModel.showSnackBarInt.value = R.string.location_permission_denied
        }

    @SuppressLint("SuspiciousIndentation")
    private val locationSettingPermissionResultRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // getMyCurrentLocation()
            } else
                _viewModel.showErrorMessage.value =
                    getString(R.string.deny_to_open_location)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveReminderBinding.inflate(layoutInflater)
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSelectLocation.setOnClickListener {
            findNavController().navigate(R.id.selectLocationFragment)
        }
        binding.fabSaveReminder.setOnClickListener {
            requestPermissionsAndAddGeofence()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermissionsAndAddGeofence() {
        if(isForegroundAndBackgroundPermissionGranted()){
            if(isDeviceLocationEnabled()){
                addGeofence()
            }else{
                enableDeviceLocation()
            }
        }else{
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
            addGeofence()
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

    private fun addGeofence() {
        TODO("Not yet implemented")
    }
}