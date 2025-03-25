package com.qy.cloud.archcore.coroutines.extensions

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt

fun TextView.modifyTextWithCondition(
    originalText: String,
    textToModify: String,
    bold: Boolean = false,
    @ColorInt color: Int,
    onClickListener: (() -> Unit)
) {
    val spannableBuilder = SpannableStringBuilder(originalText)
    val startIndex = originalText.indexOf(textToModify)

    if (startIndex != -1) {
        val endIndex = startIndex + textToModify.length

        // 加粗
        if (bold) {
            spannableBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        //添加点击事件
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClickListener.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.bgColor = Color.TRANSPARENT;
                ds.setColor(color);
            }
        }
        spannableBuilder.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = Color.TRANSPARENT
        text = spannableBuilder
    }
}

fun TextView.modifyTextWithLink(
    originalText: String,
    textToModify1: String,
    textToModify2: String,
    @ColorInt color: Int,
    onClickListener1: ((String) -> Unit),
    onClickListener2: ((String) -> Unit),
) {
    val stringBuilder = SpannableStringBuilder(originalText)

    // 处理文本1
    val startIndex1 = originalText.indexOf(textToModify1)
    if (startIndex1 != -1) {
        val endIndex1 = startIndex1 + textToModify1.length
        stringBuilder.setSpan(
            ForegroundColorSpan(color),
            startIndex1,
            endIndex1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClickListener1.invoke(textToModify1)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = color
            }
        }
        stringBuilder.setSpan(
            clickableSpan,
            startIndex1,
            endIndex1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    // 处理文本2
    val startIndex2 = originalText.indexOf(textToModify2)
    if (startIndex2 != -1) {
        val endIndex2 = startIndex2 + textToModify2.length
        stringBuilder.setSpan(
            ForegroundColorSpan(color),
            startIndex2,
            endIndex2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClickListener2.invoke(textToModify2)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = color
            }
        }
        stringBuilder.setSpan(
            clickableSpan,
            startIndex2,
            endIndex2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
    text = stringBuilder
}