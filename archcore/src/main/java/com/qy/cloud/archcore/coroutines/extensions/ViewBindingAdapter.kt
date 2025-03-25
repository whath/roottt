package com.qy.cloud.archcore.coroutines.extensions

import android.view.View
import androidx.annotation.DimenRes
import com.qy.cloud.archcore.coroutines.DebouncedClickListener

object ViewBindingAdapter {
    @JvmStatic
    @androidx.databinding.BindingAdapter("clickWithDebounce")
    fun clickWithDebounce(
        view: View,
        debouncedClickListener: DebouncedClickListener?,
    ) {
        view.setOnClickListener(debouncedClickListener)
    }

    @JvmStatic
    @androidx.databinding.BindingAdapter("isSelected")
    fun isSelected(
        view: View,
        isSelected: Boolean,
    ) {
        view.isSelected = isSelected
    }

    @JvmStatic
    @androidx.databinding.BindingAdapter("paddingBottomRes")
    fun bindPaddingBottom(
        view: View,
        @DimenRes paddingBottomRes: Int? = null,
    ) {
        val paddingBottom = paddingBottomRes?.let {
            view.context.resources.getDimensionPixelSize(it)
        } ?: 0
        view.setPaddingRelative(view.paddingStart, view.paddingTop, view.paddingEnd, paddingBottom)
    }
}
