package com.qy.cloud.network.logger

import timber.log.Timber

fun startLogger() {
    Timber.plant(Timber.DebugTree())
}

/**
 * Logs a verbose message with [Timber].
 */
inline fun logV(lambda: () -> String) = Timber.v(lambda())

/**
 * Logs a debug message with [Timber].
 */
inline fun logD(lambda: () -> String) = Timber.d(lambda())

/**
 * Logs an warning message with [Timber].
 */
inline fun logW(lambda: () -> String) = Timber.w(lambda())

/**
 * Logs an info message with [Timber].
 */
inline fun logI(lambda: () -> String) = Timber.i(lambda())

/**
 * Logs an error message with [Timber].
 *
 * @param throwable The [Throwable] if present.
 * @param message a personalized message to help to understand the error.
 */
inline fun logE(
    throwable: Throwable? = null,
    message: () -> String? = { null },
) = throwable
    ?.let { Timber.e(throwable, message() ?: "") }
    ?: run { Timber.e(message() ?: "Exception without message") }

/**
 * Logs a functional non-fatal with firebase with underlying technical error.
 *
 * @param technicalError underlying technical error for the non-fatal. For example, for a payment
 * failure non-fatal underlying technical error could be payment refused, denied by bank, cancelled
 * by user or else.
 * @param lambda non-fatal exception to throw
 */
@Deprecated("Please rely on Datadog for NonFatal records")
inline fun logNonFatal(
    technicalError: String = "",
    lambda: () -> Throwable,
) {
    logE(lambda()) { "logNonFatal" }
    try {
        if (technicalError.isNotEmpty()) {
            logD { "Failure Reason: $technicalError" }
        }
    } catch (e: IllegalStateException) {
        logE(e)
    }
}
