package com.udacity.project4

import android.app.Application
import com.udacity.project4.data.source.local.LocalDB
import com.udacity.project4.koin.repoModule
import com.udacity.project4.koin.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    val dataBaseModule = module {
        single {
            LocalDB.createRemindersDao(this@MyApp)
        }
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@MyApp)
            logger(AndroidLogger())
            modules(
                viewModelModule,
                repoModule,
                dataBaseModule
            )
        }
    }
}