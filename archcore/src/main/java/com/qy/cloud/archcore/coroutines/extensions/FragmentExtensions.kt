package com.qy.cloud.archcore.coroutines.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment

inline fun <fragment : Fragment> fragment.putArgs(argsBuilder: Bundle.() -> Unit): fragment =
    this.apply { arguments = Bundle().apply(argsBuilder) }

fun Fragment.isDisplaying() = activity != null && isAdded && !isDetached && !isRemoving
