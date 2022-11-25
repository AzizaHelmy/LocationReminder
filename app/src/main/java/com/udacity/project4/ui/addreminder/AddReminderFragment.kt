package com.udacity.project4.ui.addreminder
/**
 * Created by Aziza Helmy on 11/18/2022.
 */
import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentAddReminderBinding


class AddReminderFragment : Fragment() {
    private lateinit var binding: FragmentAddReminderBinding
    private val locationRequest =
        LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    private val locationPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                Toast.makeText(
                    requireContext(), "Location Permission granted", Toast.LENGTH_SHORT
                ).show()
               // getMyCurrentLocation()
            } else Toast.makeText(
                requireContext(), "Location Permission Denied", Toast.LENGTH_SHORT
            ).show()
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentAddReminderBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSelectLocation.setOnClickListener {
            findNavController().navigate(R.id.selectLocationFragment)
        }
        binding.fabSaveReminder.setOnClickListener {

        }
    }

}