package com.qy.cloud.archcore.coroutines.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.util.Locale

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun ContextWrapper?.switchLanguageConfig(language: String): Context? =
    this
        ?.let {
            val config: Configuration = it.resources.configuration
            val locale = Locale(language)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocale(locale)
            config.setLocales(localeList)
            this.createConfigurationContext(config)
        } ?: run {
        this
    }

fun Context.toContextWithLocaleConfiguration(): Context {
    val lang = "EN"
    val config: Configuration = resources.configuration
    val locale = Locale(lang)
    val localeList = LocaleList(locale)
    LocaleList.setDefault(localeList)
    config.setLocales(localeList)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

/**
 * Returns the hosting activity of the context if it exists,
 * null otherwise.
 */
tailrec fun Context?.hostingActivityOrNull(): Activity? =
    when (this) {
        is Activity -> this
        else -> (this as? ContextWrapper)?.baseContext?.hostingActivityOrNull()
    }
