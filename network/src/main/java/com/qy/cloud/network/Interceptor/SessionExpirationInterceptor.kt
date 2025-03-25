package com.qy.cloud.network.Interceptor

import com.qy.cloud.network.logger.logE
import java.io.EOFException
import java.nio.charset.Charset
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer

class SessionExpirationInterceptor(
    private val sessionExpirationListener: SessionExpirationListener?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val url = request.url.toString()
        val isSessionRelatedRequest = SessionRelatedRequest.values().any { url.contains(it.path) }
        val isExcludedRequest = ExcludedRequest.values().any { url.contains(it.path) }
        val isTextContent = isPlaintext(response.body?.source()?.buffer)
        val isSmallContent =
            (response.body?.contentLength() ?: 0) <= THRESHOLD_LENGTH_CONTENT_SMALL

        if (!isSessionRelatedRequest && !isExcludedRequest && isTextContent && isSmallContent) {
            val identifierHeaderValue = response.header(IDENTIFIER_HEADER, null)
            val isAtlasResponse = identifierHeaderValue
                ?.contains(ATLAS_IDENTIFIER, ignoreCase = true) == true
            val atlasForbiddenAccess = response.code == HTTP_CODE_UNAUTHORIZED
            val responseText = getResponseText(responseBody = response.body)

            identifierHeaderValue?.let {
                if (!it.contains(ATLAS_IDENTIFIER, ignoreCase = true)) {
                    trackAtlasIdentifierHeaderRegression()
                }
            }
            when {
                isAtlasResponse && atlasForbiddenAccess -> {
                    sessionExpirationListener?.onSessionExpired()
                }
                !isAtlasResponse -> {
                    val legacyForbiddenAccess = responseText?.trim()
                        ?.contains("\"isLogged\":\"0\"", ignoreCase = true) == true

                    if (legacyForbiddenAccess) {
                        sessionExpirationListener?.onSessionExpired()
                    }
                }
            }
        }
        return response
    }

    private fun isPlaintext(buffer: Buffer?): Boolean {
        return try {
            if (buffer == null) {
                return false
            }
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(
                        codePoint,
                    )
                ) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            false // Truncated UTF-8 sequence.
        }
    }

    private fun getResponseText(responseBody: ResponseBody?): String? {
        val source = responseBody?.source()
        val buffer: Buffer? = source?.buffer
        val charset = responseBody?.contentType()?.charset(UTF8) ?: UTF8

        source?.request(Long.MAX_VALUE) // Buffer the entire body.
        return buffer?.clone()?.readString(charset)
    }

    private fun trackAtlasIdentifierHeaderRegression() {
        val warningMessage = "Session expiration -" +
            " Alert! The identifier header does not contain the 'atlas' value anymore"

        logE { warningMessage }
    }

    interface SessionExpirationListener {
        fun onSessionExpired()
    }

    /**
     * Will be raised if we don't have the identifier header value containing "atlas" anymore to detect
     * an Atlas session expiration.
     */
    class AtlasIdentifierHeaderChanged(message: String) : Exception(message)

    private enum class SessionRelatedRequest(val path: String) {
        LOGOUT_WS_LEGACY_NAME(path = "userDisconnect"),
        SESSIONS_WS_ATLAS_NAME(path = "sessions"),
        USER_LOGIN_WS_LEGACY_NAME(path = "userLogin"),
    }

    private enum class ExcludedRequest(val path: String) {
        NOTIFICATION_FEED_COUNTER("notification-feed-counter"),
    }

    companion object {
        private const val API_ENDPOINT = "endPoint"
        private const val API_RESPONSE_EXPIRATION = "apiResponse"
        private const val NOT_AVAILABLE = "null/not available"
        private const val IDENTIFIER_HEADER = "identifier"
        private const val ATLAS_IDENTIFIER = "atlas"
        private const val THRESHOLD_LENGTH_CONTENT_SMALL = 500L
        private const val HTTP_CODE_UNAUTHORIZED = 401
        private val UTF8 = Charset.forName("UTF-8")
    }
}
