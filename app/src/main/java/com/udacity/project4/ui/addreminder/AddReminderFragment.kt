package com.udacity.project4.ui.addreminder
/**
 * Created by Aziza Helmy on 11/18/2022.
 */
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentAddReminderBinding


class AddReminderFragment : Fragment() {
    private lateinit var binding: FragmentAddReminderBinding
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