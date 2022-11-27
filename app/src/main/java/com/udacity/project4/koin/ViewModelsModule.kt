package com.udacity.project4.koin

import com.udacity.project4.data.source.ReminderDataSource
import com.udacity.project4.ui.reminderlist.RemindersListViewModel
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by Aziza Helmy on 11/26/2022.
 */

val viewModelModule = module {
    //Get the view model this time as a single to be shared with the another fragment
    single {
        SaveReminderViewModel(get(), get() as ReminderDataSource)
    }
    viewModel {
        RemindersListViewModel(get(), get() as ReminderDataSource)
    }
}