package com.qy.cloud.network.env

import com.qy.cloud.network.BuildConfig

/**
 * Singleton used to hold URLs and credentials of 3rd party libraries used throughout the
 * application, that can be updated according to the environment type (prod, staging, etc).
 */
object AppEnvironment {

    private const val BUILD_TYPE_DEBUG = "debug"

    private const val BUILD_TYPE_RELEASE = "release"

    @JvmStatic
    var environment: AppEnvironmentType =
        when (BuildConfig.BUILD_TYPE) {
            BUILD_TYPE_DEBUG -> AppEnvironmentType.STAGING
            else -> AppEnvironmentType.RELEASE
        }
        private set

    val isBuildTypeRelease
        get() = BuildConfig.BUILD_TYPE == BUILD_TYPE_RELEASE

    val isBuildTypeDebug
        get() = BuildConfig.BUILD_TYPE == BUILD_TYPE_DEBUG

    val debugLogsEnabled: Boolean
        get() = BuildConfig.BUILD_TYPE != BUILD_TYPE_RELEASE

    val isEnvProd
        get() = environment == AppEnvironmentType.RELEASE

    val isEnvStaging
        get() = environment == AppEnvironmentType.STAGING

}
