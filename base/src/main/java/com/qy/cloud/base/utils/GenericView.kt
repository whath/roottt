package com.qy.cloud.base.utils

interface GenericView {
    fun showError(
        message: String? = null,
        onDismiss: (() -> Unit)? = null,
    )

    fun showInfo(
        message: String,
        onDismiss: (() -> Unit)? = null,
    )
}
