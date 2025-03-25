package com.qy.cloud.archcore.coroutines.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Response

class RetrofitResponseResult<Data>(
    var response: Response<Data>? = null,
    var success: Boolean = false,
    var data: Data? = null,
    var error: RetrofitErrorResponse? = null,
    var redirection: String? = null,
) {

    suspend fun onSuccess(
        successDispatcher: CoroutineDispatcher? = null,
        listener: suspend (Data?) -> Unit,
    ) {
        if (success) {
            successDispatcher?.let { dispatcher ->
                withContext(dispatcher) {
                    listener(data)
                }
            } ?: run {
                listener(data)
            }
        }
    }

    suspend fun onError(
        errorDispatcher: CoroutineDispatcher? = null,
        listener: suspend (RetrofitErrorResponse?) -> Unit,
    ) {
        if (!success) {
            errorDispatcher?.let { dispatcher ->
                withContext(dispatcher) {
                    listener(error)
                }
            } ?: run {
                listener(error)
            }
        }
    }
}
