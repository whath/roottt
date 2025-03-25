package com.qy.cloud.base

import android.content.Context
import android.content.Intent

/**
 * Interface to use features present in :app  that needs to be accessible
 * by module :base.
 *
 * Once a feature is modularized, a function can be deleted.
 */
interface BaseFeaturesNavigator {

    fun getAccessActivityClass(): Class<*>

}
