package com.qy.cloud.network.Interceptor

import java.io.IOException
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import java.util.HashMap
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AddCookiesInterceptor(private val cookieManager: CookieManager?) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        val baseUri = URI
            .create("${chain.request().url.scheme}://${chain.request().url.host}")

        // Fetch cookies from cookie manager and put them into request
        if (cookieManager != null) {
            val cookiesList = cookieManager.cookieStore.get(baseUri)
            val cookiesStr = getCookie(cookiesList)
            if (cookiesStr.isNotEmpty()) {
                builder.addHeader("Cookie", cookiesStr)
                Timber.d("Adding cookie: $cookiesStr")
            }
        }

        val response = chain.proceed(builder.build())

        // Save cookies from response into cookie manager
        if (cookieManager != null) {
            val cookies = response.headers("Set-Cookie")
            val map = HashMap<String, List<String>>()
            map["Set-Cookie"] = cookies
            cookieManager.put(baseUri, map)
        }

        return response
    }

    private fun getCookie(cookiesList: List<HttpCookie?>?): String {
        var cookiesStr = StringBuilder()
        cookiesList?.forEach { cookie ->
            cookie?.apply {
                cookiesStr = cookiesStr.append(this.name).append("=").append(this.value).append(";")
            }
        }
        return cookiesStr.toString()
    }
}
