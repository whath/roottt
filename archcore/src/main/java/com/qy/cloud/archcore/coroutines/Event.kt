package com.qy.cloud.archcore.coroutines

import androidx.compose.runtime.Immutable

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * Use this class if you want to pass event along with information between view models and
 * views.
 */
@Immutable
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
