package com.qy.cloud

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.qy.cloud.base.QlyAppLifecycleObserver
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module

class QlyApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        startDI()
    }

    override fun onCreate() {
        super.onCreate()
        observeAppLifecycle()
    }

    private fun observeAppLifecycle() {
        with(ProcessLifecycleOwner.get().lifecycle) {
            addObserver(QlyAppLifecycleObserver())
        }
    }

    private fun startDI() {
        val listOfModules = mutableListOf<Module>()

        val loggerLevel = Level.INFO

        startKoin {
            androidLogger(loggerLevel)
            androidContext(this@QlyApplication)
            // Modules
            modules(listOfModules)
        }
    }
}