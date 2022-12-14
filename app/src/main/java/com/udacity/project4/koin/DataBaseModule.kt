package com.udacity.project4.koin

import com.udacity.project4.data.source.local.LocalDB
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Created by Aziza Helmy on 11/26/2022.
 */
val dataBaseModule = module {
    single {
        LocalDB.createRemindersDao(androidApplication())
    }
}