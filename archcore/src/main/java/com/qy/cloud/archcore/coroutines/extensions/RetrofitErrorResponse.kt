package com.qy.cloud.archcore.coroutines.extensions

class RetrofitErrorResponse(
    val errorCode: Int = -1,
    message: String? = null,
    cause: Throwable? = null,
    val rawErrorBody: String? = null,
    val errorBodyMeta: Any? = null,
) : Exception(message, cause)
