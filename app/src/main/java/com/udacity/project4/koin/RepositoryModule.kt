package com.udacity.project4.koin

import com.udacity.project4.data.repo.RemindersLocalRepository
import com.udacity.project4.data.source.ReminderDataSource
import org.koin.dsl.module

/**
 * Created by Aziza Helmy on 11/26/2022.
 */
val repoModule = module {
    single {
        RemindersLocalRepository(get()) as ReminderDataSource
    }
}