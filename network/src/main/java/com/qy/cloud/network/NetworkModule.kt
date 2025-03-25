package com.qy.cloud.network

import android.content.Context
import com.qy.cloud.network.Interceptor.SessionExpirationInterceptor
import com.qy.cloud.network.cookie.PersistentCookieStore
import com.qy.cloud.network.util.PhoneUtils
import okhttp3.Cache
import java.net.CookieManager
import java.net.CookiePolicy

object NetworkModule {
    private var sessionExpirationListener: SessionExpirationInterceptor.SessionExpirationListener? = null
    internal var baseParamsApi: BaseParamsApi? = null
        private set

    /**
     * Retrieve CookieManager from other part of app
     * Supposed to be temp since we ll retrieve cookies from our new module
     * @param legacyCookieManager cookies manager
     */
    var cookieManager: CookieManager? = null

    fun init(
        context: Context,
        platform: String?,
        appVersion: String?,
        lang: String?,
        currency: String?,
        siteId: String?,
        countryIsoCode: String?,
        sessionExpirationListener: SessionExpirationInterceptor.SessionExpirationListener?,
    ) {
        initCookies(context)
        baseParamsApi = BaseParamsApi(
            lang = "EN",
        ).apply {
            timezoneOffset = PhoneUtils.getTimezoneOffsetWithoutDstInMinutes()
            this.countryIsoCode = countryIsoCode ?: PhoneUtils.getCountryIsoCode(context) ?: ""
        }
        this.sessionExpirationListener = sessionExpirationListener
    }

    fun initCookies(context: Context) {
        cookieManager = CookieManager(PersistentCookieStore(context), CookiePolicy.ACCEPT_ALL)
    }

    fun isValidSessionId() = baseParamsApi?.sessionId != null

    fun <T> generateGenericService(
        context: Context,
        legacy: Boolean = false,
        serviceClass: Class<T>,
        baseUrl: String,
        addBaseParamsApi: Boolean? = null,
        addCookieManager: Boolean? = null,
        addCache: Boolean? = null,
        useKotlinCoroutines: Boolean = false,
        supportUnknownEnums: Boolean = false,
        addExpirationInterceptor: Boolean = false,
    ): T {
        val expirationListener = if (addExpirationInterceptor) sessionExpirationListener else null
        val paramsApi = if (addBaseParamsApi == true) baseParamsApi else null
        val manager = if (addCookieManager == true) cookieManager else null
        val cache = if (addCache == true) Cache(context.cacheDir, 30 * 1024 * 1024) else null
        return ServiceApiGenerator.createService(
            legacy = legacy,
            serviceClass = serviceClass,
            baseUrl = baseUrl,
            baseParamsApi = paramsApi,
            cookieManager = manager,
            cache = cache,
            sessionExpirationListener = expirationListener,
            useKotlinCoroutines = useKotlinCoroutines,
            supportUnknownEnums = supportUnknownEnums,
        )
    }
}