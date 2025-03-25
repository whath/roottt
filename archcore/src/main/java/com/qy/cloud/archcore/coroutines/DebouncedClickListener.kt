package com.qy.cloud.archcore.coroutines

import android.os.SystemClock
import android.view.View
import com.qy.cloud.archcore.coroutines.extensions.logD

class DebouncedClickListener(
    private val action: () -> Unit,
) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        logD { "onClick called on Listener instance $this, at time :${SystemClock.elapsedRealtime()}" }
        if (SystemClock.elapsedRealtime() - lastClickTime < DEFAULT_DEBOUNCE_TIME) {
            return
        } else {
            action()
        }
        lastClickTime = SystemClock.elapsedRealtime()
    }

    companion object {
        private const val DEFAULT_DEBOUNCE_TIME = 1000L
    }
}
