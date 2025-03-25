package com.qy.cloud.network

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.qy.cloud.network.Interceptor.AddCookiesInterceptor
import com.qy.cloud.network.Interceptor.ChuckerProvider
import com.qy.cloud.network.Interceptor.CurlLoggingInterceptor
import com.qy.cloud.network.Interceptor.SessionExpirationInterceptor
import com.qy.cloud.network.retrofitTool.CetDateJacksonConverterFactory
import com.qy.cloud.network.retrofitTool.RxCallAdapterFactory
import java.net.CookieManager
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinComponent
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import timber.log.Timber

/**
 * Created by david.d on 24/01/2017.
 * Service API oriented generator
 */
object ServiceApiGenerator : KoinComponent {

    private const val DEFAULT_REQUEST_TIMEOUT_MS = 20_000L
    private const val PHOTOS_UPLOAD_REQUEST_TIMEOUT_MS = 60_000L

    private val loggingInterceptor by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        interceptor
    }

    val objectMapper by lazy {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule.Builder().build())
        mapper.dateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
        mapper
    }

    private fun buildBaseParamsInterceptor(
        baseParamsApi: BaseParamsApi,
        legacy: Boolean,
    ): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            val builderUrl = request.url.newBuilder()
            if (legacy) {
                baseParamsApi.baseParams.entries.forEach {
                    builderUrl.addQueryParameter(it.key, it.value)
                }
            }
            val url = builderUrl.build()

//            val userAgent = "Vestiaire Collective/${baseParamsApi.baseParams["androidVersion"]} " +
//                "(${Build.PRODUCT}:${Build.HARDWARE} Android:${Build.VERSION.SDK_INT})"

            // Request customization: add request headers
            val requestBuilder = request.newBuilder()

            with(requestBuilder) {
//                addHeader("User-Agent", userAgent)
                if (!(request.method.toUpperCase() == "POST" && url.encodedPath
                        .toLowerCase() == "/sessions")
                ) {
                    baseParamsApi.sessionId
                        ?.let {
                            if (it.isNotBlank()) {
                                addHeader("__session", it)
                            } else {
                                logMissingSession(it, url)
                            }
                        }
                        ?: run { logMissingSession(null, url) }
                }
//                    baseParamsApi.baseParams["id_site"]?.let { addHeader("X-VC-SiteId", it) }
//                    baseParamsApi.baseParams["lang"]?.let { addHeader("X-VC-Language", it) }
//                    baseParamsApi.baseParams["currency"]?.let { addHeader("X-VC-Currency", it) }
                addHeader("X-VC-Country", baseParamsApi.countryIsoCode)
                url(url)
                chain.proceed(build())
            }
        }
    }

    private fun logMissingSession(session: String?, url: HttpUrl) {
        val message = "No session on header - session = [$session], url = [$url]"
        Timber.d(message)
    }

    fun <S> createService(
        legacy: Boolean,
        serviceClass: Class<S>,
        baseUrl: String,
        baseParamsApi: BaseParamsApi?,
        cookieManager: CookieManager?,
        cache: Cache? = null,
        sessionExpirationListener: SessionExpirationInterceptor.SessionExpirationListener? = null,
        useKotlinCoroutines: Boolean = false,
        supportUnknownEnums: Boolean = false,
    ): S {
        val httpClientBuilder = OkHttpClient.Builder()
        cache?.let { httpClientBuilder.cache(it) }
        baseParamsApi?.let {
            httpClientBuilder.addInterceptor(
                buildBaseParamsInterceptor(it, legacy)
            )
        }
        cookieManager?.let {
            httpClientBuilder.addInterceptor(AddCookiesInterceptor(cookieManager))
        }
//        if (AppEnvironment.debugLogsEnabled) {
//            httpClientBuilder.addInterceptor(loggingInterceptor)
//        }
//        if (AppEnvironment.isBuildTypeDebug) {
        httpClientBuilder.addInterceptor(CurlLoggingInterceptor())
//        }
//        if (!AppEnvironment.isBuildTypeRelease) {
//            httpClientBuilder.addInterceptor(ChuckerProvider.getChuckerInterceptor())
//        }
        httpClientBuilder.readTimeout(DEFAULT_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        httpClientBuilder.writeTimeout(DEFAULT_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        httpClientBuilder.connectTimeout(DEFAULT_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)

        return createService(
            serviceClass,
            baseUrl,
            httpClientBuilder,
            objectMapper,
            sessionExpirationListener,
            useKotlinCoroutines,
            supportUnknownEnums,
        )
    }

    fun <S> createPhotosUploadService(
        serviceClass: Class<S>,
        baseUrl: String,
        baseParamsApi: BaseParamsApi?,
        cookieManager: CookieManager?,
        cache: Cache? = null,
        sessionExpirationListener: SessionExpirationInterceptor.SessionExpirationListener? = null,
        useKotlinCoroutines: Boolean = false,
        supportUnknownEnums: Boolean = false,
    ): S {
        val httpClientBuilder = OkHttpClient.Builder()

        cache?.let { httpClientBuilder.cache(it) }
        baseParamsApi?.let {
            httpClientBuilder.addInterceptor(
                buildBaseParamsInterceptor(it, false),
            )
        }
        cookieManager?.let {
            httpClientBuilder.addInterceptor(AddCookiesInterceptor(cookieManager))
        }
//        if (AppEnvironment.debugLogsEnabled) {
        httpClientBuilder.addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            },
        )
//        }
//        if (!AppEnvironment.isBuildTypeRelease) {
//            httpClientBuilder.addInterceptor(ChuckerProvider.getChuckerInterceptor())
//        }

        httpClientBuilder.readTimeout(PHOTOS_UPLOAD_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        httpClientBuilder.writeTimeout(PHOTOS_UPLOAD_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        httpClientBuilder.connectTimeout(PHOTOS_UPLOAD_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)

        return createService(
            serviceClass,
            baseUrl,
            httpClientBuilder,
            objectMapper,
            sessionExpirationListener,
            useKotlinCoroutines,
            supportUnknownEnums,
        )
    }

    private fun <S> createService(
        serviceClass: Class<S>,
        baseUrl: String,
        httpClientBuilder: OkHttpClient.Builder,
        objectMapper: ObjectMapper,
        sessionExpirationListener: SessionExpirationInterceptor.SessionExpirationListener? = null,
        useKotlinCoroutines: Boolean = false,
        supportUnknownEnums: Boolean = false,
    ): S {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        httpClientBuilder.addInterceptor(loggingInterceptor)
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                JacksonConverterFactory.create(
                    objectMapper.copy().apply {
                        if (supportUnknownEnums) {
                            enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                        }
                    },
                ),
            )
            .apply {
                if (!useKotlinCoroutines) {
                    addCallAdapterFactory(RxCallAdapterFactory.create()) // to use Rx2 + specific errors handling
                }
            }

        return builder.client(httpClientBuilder.build()).build().create(serviceClass)
    }
}
