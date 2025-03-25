package com.qy.cloud.base

import android.app.Application
import android.content.pm.ApplicationInfo
import android.webkit.WebView
import com.qy.cloud.network.NetworkModule
import com.qy.cloud.network.env.AppEnvironment
import com.qy.cloud.network.logger.logD
import com.qy.cloud.network.logger.startLogger


abstract class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (AppEnvironment.debugLogsEnabled) {
            startLogger()
        }
        initWebViewDebugging()
        initNetworkModule()

    }

    private fun initNetworkModule() {
        NetworkModule.init(
            context = this,
            platform = null,
            appVersion = null,
            lang = null,
            currency = null,
            siteId = null,
            countryIsoCode = null,
            sessionExpirationListener = null,
        )
    }

    protected open fun initWebViewDebugging() {
        logD { "initWebViewDebugging" }
        if (AppEnvironment.debugLogsEnabled) {
            if (0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }
    }

    companion object {
        var instance: BaseApplication? = null

        @JvmStatic
        fun get(): BaseApplication {
            return instance as BaseApplication
        }
    }
}
