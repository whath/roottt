@file:Suppress("MaxLineLength", "LongParameterList")

package com.qy.cloud.network.env

enum class AppEnvironmentType(
    val baseUrl: String
) {
    STAGING(
        baseUrl = ""
    ),

    RELEASE(
        baseUrl = ""
    ),
}
