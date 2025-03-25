package com.qy.cloud.archcore.coroutines.extensions

data class ResponseErrorBody<T>(
    val errors: Errors,
    val meta: T?,
)
