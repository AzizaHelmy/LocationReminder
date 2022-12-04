package com.udacity.project4.ui.reminderdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.ui.reminderlist.ReminderDataItem

class ReminderDescriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReminderDescriptionBinding

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.hasExtra(EXTRA_ReminderDataItem)) {
            binding.reminderDataItem =
                intent.getParcelableExtra<ReminderDataItem>(EXTRA_ReminderDataItem)
        }
    }
}