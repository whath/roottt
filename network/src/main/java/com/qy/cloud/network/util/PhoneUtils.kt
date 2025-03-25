package com.qy.cloud.network.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.TelephonyManager
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object PhoneUtils {

    fun call(
        context: Context?,
        phoneNumber: String,
    ) {
        context?.let {
            it.startActivity(
                Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                },
            )
        }
    }

    /***
     * Here we add " - calendar.timeZone.dstSavings" because we want to get rid of the daylight
     * savings time notion for countries supporting it.
     */
    fun getTimezoneOffsetWithoutDstInMinutes(): String =
        Calendar.getInstance()
            .let { calendar ->
                val zoneOffSet = calendar.get(Calendar.ZONE_OFFSET)
                val dstOffSet = calendar.get(Calendar.DST_OFFSET)
                val minuteToMillis = TimeUnit.MINUTES.toMillis(1)
                val daylightSavingsOffset =
                    if (calendar.timeZone.inDaylightTime(calendar.time)) {
                        calendar.timeZone.dstSavings
                    } else {
                        0
                    }
                -(zoneOffSet + dstOffSet - daylightSavingsOffset) / minuteToMillis
            }
            .toString()

    internal fun normalizeCountryCode(countryCode: String?): String? =
        countryCode?.let { country ->
            Normalizer.normalize(country, Normalizer.Form.NFD)
                ?.replace("[^\\p{ASCII}]".toRegex(), "")
        }

    /***
     * Get the country iso code from the device.
     */
    fun getCountryIsoCode(context: Context): String? {
        var countryCode: String? = null

        (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.let { tm ->
            try {
                countryCode = when (tm.simCountryIso?.length == 2) {
                    true -> tm.simCountryIso.toUpperCase(Locale.getDefault())
                    else -> {
                        when (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) {
                            true -> {
                                when (tm.networkCountryIso?.length == 2) {
                                    true -> tm.networkCountryIso.toUpperCase(Locale.getDefault())
                                    else -> null
                                }
                            }
                            else -> null
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }
        return normalizeCountryCode(countryCode)
    }
}
