package com.qy.cloud.network

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Created by david.d on 26/01/2017.
 * Base query params sent in each request
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class BaseParamsApi(
    lang: String,
) {

    val baseParams: MutableMap<String, String>
    var sessionId: String? = null
    var timezoneOffset: String = ""
    var countryIsoCode: String = ""

    init {
        val paramValues = arrayOf(lang)
        baseParams = baseParamKeys.zip(paramValues).toMap().toMutableMap()
    }

    companion object {
        val baseParamKeys =
            arrayOf("u", "v", "h", "a", "androidVersion", "lang", "currency", "id_site")
    }
}
