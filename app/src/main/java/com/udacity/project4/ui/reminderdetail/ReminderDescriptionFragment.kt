package com.udacity.project4.ui.reminderdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentReminderDescriptionBinding
import com.udacity.project4.ui.reminderlist.ReminderDataItem


class ReminderDescriptionFragment : Fragment() {
    private lateinit var binding: FragmentReminderDescriptionBinding

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionFragment::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentReminderDescriptionBinding.inflate(layoutInflater)

        return binding.root
    }

}