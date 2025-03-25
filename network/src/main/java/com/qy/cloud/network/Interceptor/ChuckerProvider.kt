package com.qy.cloud.network.Interceptor

import com.chuckerteam.chucker.api.ChuckerInterceptor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ChuckerProvider : KoinComponent {
    fun getChuckerInterceptor(): ChuckerInterceptor {
        val chuckerInterceptorProvider by inject<ChuckerInterceptorProvider>()
        return chuckerInterceptorProvider.get()
    }
}
