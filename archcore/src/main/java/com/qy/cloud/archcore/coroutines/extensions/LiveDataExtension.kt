package com.qy.cloud.archcore.coroutines.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.qy.cloud.archcore.coroutines.Event

fun <T> LiveData<Event<T>>.handleEventOnce(
    owner: LifecycleOwner,
    observer: (t: T) -> Unit,
) {
    observe(owner) { it?.getContentIfNotHandled()?.let(observer) }
}

fun <T> LiveData<Event<T>>.handleEvent(
    owner: LifecycleOwner,
    observer: (t: T) -> Unit,
) {
    observe(owner) { it?.peekContent()?.let(observer) }
}
