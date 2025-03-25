package com.qy.cloud.archcore.coroutines.extensions

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.delay
import retrofit2.HttpException
import retrofit2.Response

/**
 * Trigger an api call with retrials implemented as exponential-backoff
 * https://en.wikipedia.org/wiki/Exponential_backoff
 * @param retries number of retrials
 * @param initialDelay for backoff
 * @param factor exponential factor
 * @param call api call
 * @return [RetrofitResponseResult] success if successful within retry limit else an error
 */
suspend fun <Data : Any> safeCoroutinesApiCallWithRetrials(
    retries: Int = DEFAULT_NUMBER_OF_RETRIALS,
    initialDelay: Long = DEFAULT_INITIAL_DELAY,
    factor: Double = 2.0,
    call: suspend () -> Response<Data>,
): RetrofitResponseResult<Data> {
    // Initializations
    var currentDelay = initialDelay
    var result = RetrofitResponseResult<Data>(
        error =
            RetrofitErrorResponse(message = "Not Running the request, default retrials is -1"),
    )
    // Retrial Logic
    for (i in 0..retries) {
        result = safeCoroutinesApiCall(call)
        if (result.error != null) { // Retry
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        } else { // Exit with result
            return result
        }
    }
    return result
}

/**
 * Wrapper to handle network responses done via Retrofit 2.6.0 or higher which supports suspend
 * keyword.
 */
suspend fun <Data : Any> safeCoroutinesApiCall(
    call: suspend () -> Response<Data>,
): RetrofitResponseResult<Data> =
    try {
        processResult(call.invoke())
    } catch (e: HttpException) {
        RetrofitResponseResult<Data>().apply {
            error = RetrofitErrorResponse(errorCode = e.code(), message = e.message(), cause = e)
        }
    } catch (e: Throwable) {
        RetrofitResponseResult<Data>().apply { error = RetrofitErrorResponse(cause = e) }
    }

/**
 * Invokes an API request and captures the response successfully only if a 30X.
 * Further, extract "location" header value to find the redirection resource.
 * This value is sent back to the caller as a successful response. In all other cases,
 * an error is returned (20X, 40x etc.)
 *
 * Note: set auto-redirect off for the OkHttp Client
 */
suspend fun <Data : Any> launchRedirectingAPI(
    call: suspend () -> Response<Data>,
): RetrofitResponseResult<Data> =
    try {
        val result = RetrofitResponseResult<Data>()
        val callResponse = call.invoke()
        with(callResponse) {
            val locationHeaderValue = when (code()) {
                301, 302, 303, 307, 308 -> headers().values("location")
                else -> null
            }

            locationHeaderValue?.let { values ->
                val redirectingUrl = values.find {
                    it != null
                }
                redirectingUrl?.let {
                    result.apply {
                        response = callResponse
                        success = true
                        redirection = redirectingUrl
                    }
                } ?: kotlin.run {
                    result.error =
                        RetrofitErrorResponse(errorCode = code(), message = "No redirecting URL")
                }
            } ?: kotlin.run {
                result.error =
                    RetrofitErrorResponse(errorCode = code(), message = "No redirecting URL")
            }
        }
        result
    } catch (e: HttpException) {
        RetrofitResponseResult<Data>().apply {
            error = RetrofitErrorResponse(errorCode = e.code(), message = e.message(), cause = e)
        }
    } catch (e: Throwable) {
        RetrofitResponseResult<Data>().apply { error = RetrofitErrorResponse(cause = e) }
    }

/***
 * Convert a raw error body as a string to the desired object.
 * @param rawErrorBody Raw body representing the error as a string.
 * @return An object of the reified type or null if it couldn't be parsed.
 */
inline fun <reified Data> parseErrorBody(rawErrorBody: String?): Data? =
    rawErrorBody?.let {
        try {
            Gson().fromJson(
                it,
                Data::class.java,
            )
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }

private fun <Data : Any> processResult(callResponse: Response<Data>): RetrofitResponseResult<Data> {
    with(callResponse) {
        val result = RetrofitResponseResult<Data>()
        result.apply {
            response = callResponse
            success = isSuccessful

            body()?.let { body ->
                data = body
            }
        }
        if (!isSuccessful) {
            val rawErrorBody = errorBody()?.string()
            val errorBody = rawErrorBody?.let { body ->
                Gson().fromJson(body, ResponseErrorBody::class.java)
            }
            result.error = RetrofitErrorResponse(
                errorCode = code(),
                message = errorBody?.errors?.detail ?: message(),
                rawErrorBody = rawErrorBody,
                errorBodyMeta = errorBody?.meta,
            )
        }
        return result
    }
}

private const val DEFAULT_NUMBER_OF_RETRIALS = 3
private const val DEFAULT_INITIAL_DELAY = 100L // 0.1s
