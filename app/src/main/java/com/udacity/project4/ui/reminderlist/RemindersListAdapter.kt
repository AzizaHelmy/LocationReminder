package com.udacity.project4.ui.reminderlist

import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter

//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.reminder_item
}