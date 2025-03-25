package com.qy.cloud.archcore.coroutines.extensions

import kotlinx.coroutines.CancellationException
import java.net.ConnectException
import java.net.UnknownHostException
import com.qy.cloud.archcore.coroutines.Result

fun Result.Error.loggableError(): Boolean = loggableError(exception)

fun loggableError(exception: Throwable?): Boolean =
    when (exception) {
        is UnknownHostException -> false
        is ConnectException -> false
        is CancellationException -> false
        // Add more!
        else -> true
    }

/**
 * @param resultList list of [Result]
 * @return true if all elements in the list are [Result.Success], false otherwise
 */
fun areAllSuccess(resultList: List<Result<Any>>): Boolean =
    resultList.all { it is Result.Success }

/**
 * Returns the first error result, or null if none are present
 */
fun List<Result<*>>.firstErrorOrNull(): Result.Error? =
    firstInstanceOrNull<Result.Error>()
