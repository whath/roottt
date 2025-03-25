package com.qy.cloud.network.retrofitTool

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class CetDateJacksonConverterFactory() : Converter.Factory() {
    private val cetDateObjectMapper by lazy {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule.Builder().build())
        mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("CET")
        }
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
        mapper
    }
    private val cetDateConverter: Converter.Factory by lazy { JacksonConverterFactory.create(cetDateObjectMapper) }

    override fun responseBodyConverter(
            type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
        annotations?.let {
            for (annotation in it) {
                if (type != null && retrofit != null && annotation.annotationClass == CetDateType::class) {
                    return cetDateConverter.responseBodyConverter(type, annotations, retrofit)
                }
            }
        }
        return null
    }
}
