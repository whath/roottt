package com.qy.cloud.base.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.qy.cloud.network.logger.logE
import com.qy.cloud.base.BaseFeaturesNavigator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ActivityLauncher : KoinComponent {

    private val baseFeaturesNavigator by inject<BaseFeaturesNavigator>()

    val googlePlayIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = Uri.parse(
                "http://play.google.com/store/apps/details?id=fr.vestiairecollective",
            )
            return intent
        }

    fun startHomeActivity(context: Context) {
        val clazz = baseFeaturesNavigator.getAccessActivityClass()
        val intent = Intent(context, clazz)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    fun launchDeeplink(
        context: Context,
        link: String?,
    ) {
        if (link == null) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        if (context !is Activity) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            logE(e)
        }
    }
}
