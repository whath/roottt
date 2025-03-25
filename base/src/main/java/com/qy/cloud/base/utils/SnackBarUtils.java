package com.qy.cloud.base.utils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.qy.cloud.archcore.coroutines.extensions.LogExtensionsKt;
import com.qy.cloud.base.R;


import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Created by david.d on 08/02/2017.
 * Manager to construct SnackBar message to be displayed in CoordinatorLayout
 */
//TODO Custom snack with left icon
//TODO http://www.materialdoc.com/snackbar/
public class SnackBarUtils {

    public enum SnackBarType {
        ALERT, INFO, SUCCESS
    }

    public static void showSnackBarIndefinite(Context context, View snackBarLayout, String msg, SnackBarType snackBarType) {
        if (snackBarLayout == null || msg == null) {
            return;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(msg);
        showSnackBar(context, snackBarLayout, spannableStringBuilder, snackBarType, Snackbar.LENGTH_INDEFINITE, null);
    }

    public static void showSnackBar(Context context, View snackBarLayout, String msg, SnackBarType snackBarType) {
        showSnackBar(context, snackBarLayout, msg, snackBarType, null);
    }

    public static void showSnackBar(
        Context context,
        View snackBarLayout,
        String msg,
        SnackBarType snackBarType,
        @Nullable Function0<Unit> onDismiss
    ) {
        if (snackBarLayout == null || msg == null) {
            return;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(msg);
        showSnackBar(context, snackBarLayout, spannableStringBuilder, snackBarType, Snackbar.LENGTH_LONG, onDismiss);
    }

    private static void showSnackBar(
        Context context,
        View snackBarLayout,
        SpannableStringBuilder spannableString,
        SnackBarType snackBarType,
        int length,
        @Nullable Function0<Unit> onDismiss
    ) {
        if (context != null) {
            LogExtensionsKt.logFirebase(() -> "Showing snackbar for: " + context + ", at: " + System.currentTimeMillis());

            Snackbar snackbar = createSnackBar(context, snackBarLayout, spannableString, snackBarType, length);
            if (onDismiss != null) {
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        onDismiss.invoke();
                    }
                });
            }
            snackbar.show();
        }
    }

    private static Snackbar createSnackBar(Context context, View snackBarLayout, SpannableStringBuilder spannableString, SnackBarType snackBarType, int length) {
        Snackbar snackbar = Snackbar.make(snackBarLayout, spannableString, length);
        View snackBarView = snackbar.getView();

        int backgroundColorId = -1;
        int textColorId = -1; //white

        //Background
        switch (snackBarType) {
            case ALERT:
                backgroundColorId = ContextCompat.getColor(context, R.color.colorProblem);
                textColorId = ContextCompat.getColor(context, android.R.color.white);
                break;
            case INFO:
                backgroundColorId = ContextCompat.getColor(context, R.color.colorConfirmation);
                textColorId = ContextCompat.getColor(context, android.R.color.white);
                break;
            case SUCCESS:
                backgroundColorId = ContextCompat.getColor(context, R.color.sys_green);
                textColorId = ContextCompat.getColor(context, android.R.color.white);
                break;
        }
        if (backgroundColorId != -1) snackBarView.setBackgroundColor(backgroundColorId);

        //Text appearance
        TextView tvMessage = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tvMessage != null) {
            tvMessage.setTextColor(textColorId);
            tvMessage.setMaxLines(4);
        }
        return snackbar;
    }
}
