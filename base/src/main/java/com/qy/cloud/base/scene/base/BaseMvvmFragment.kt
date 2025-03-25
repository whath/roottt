package com.qy.cloud.base.scene.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.qy.cloud.archcore.coroutines.extensions.logFirebase
import com.qy.cloud.base.R
import com.qy.cloud.base.utils.GenericView
import com.qy.cloud.base.utils.SnackBarUtils
import com.qy.cloud.base.BaseFeaturesNavigator
import org.koin.android.ext.android.inject

abstract class BaseMvvmFragment : Fragment(), GenericView {

    protected var appBarLayout: AppBarLayout? = null
    protected var toolbar: Toolbar? = null
    private var progressBar: ProgressBar? = null
    private var tvNbCartItems: TextView? = null

    private val baseFeaturesNavigator by inject<BaseFeaturesNavigator>()

    @get:LayoutRes
    protected abstract val layoutRes: Int
    protected abstract val shouldUseDefaultAppBarLayout: Boolean
    protected open var shouldContainProgressBar = false
    protected open val displayHomeAsUpEnabled = true
    protected open val displayShowHomeEnabled = true
    protected open val displayShowTitleEnabled = true
    protected open val displayCartItem = false
    protected open val displayMoreOptions = false
    protected open val shouldUseDefaultTracking = true

    private fun showSnackBar(
        snackBarLayout: View?,
        message: String,
        snackBarType: SnackBarUtils.SnackBarType,
        onDismiss: (() -> Unit)?,
    ) {
        val context = activity
        if (context != null) {
            SnackBarUtils.showSnackBar(context, snackBarLayout, message, snackBarType, onDismiss)
        }
    }

    protected fun showSnackBarIndefinite(
        snackBarLayout: View?,
        message: String,
        snackBarType: SnackBarUtils.SnackBarType,
    ) {
        val context = activity
        if (context != null) {
            SnackBarUtils.showSnackBarIndefinite(context, snackBarLayout, message, snackBarType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(layoutRes, container, false)
        if (shouldUseDefaultAppBarLayout) {
            when (view) {
                is CoordinatorLayout -> {
                    val appBar = inflater.inflate(R.layout.app_bar_default, view, false)
                    view.addView(appBar, 0)
                }

                is ConstraintLayout -> {
                    val appBar = inflater.inflate(R.layout.app_bar_default, view, false)
                    view.addView(appBar, 0)
                }

                else -> throw IllegalStateException(
                    "You must use CoordinatorLayout or " +
                            "ConstraintLayout if shouldUseDefaultAppBarLayout() returns true",
                )
            }
        }
        appBarLayout = view.findViewById(R.id.app_bar_layout)
        appBarLayout?.outlineProvider= null
        toolbar = view.findViewById(R.id.toolbar)
        setupProgressBar(inflater, view)
        initToolbar()

        return view
    }

    private fun setupProgressBar(
        inflater: LayoutInflater,
        view: View,
    ) {
        if (!shouldContainProgressBar) {
            return
        }
        progressBar = view.findViewById(R.id.progress_bar)
        if (progressBar != null) {
            return
        }
        if (view is CoordinatorLayout || view is FrameLayout || view is ConstraintLayout) {
            val viewGroup = view as ViewGroup
            progressBar =
                inflater.inflate(R.layout.progress_bar_default, viewGroup, false) as ProgressBar
            viewGroup.addView(progressBar)
        } else {
            throw IllegalStateException("You must put a ProgressBar in the layout xml")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun onAttach(activity: Activity) {
        if (activity !is BaseCommentActivity) {
            throw IllegalStateException("$activity must inherit from BaseVestiaireActivity")
        }
        super.onAttach(activity)
    }

    override fun showError(
        message: String?,
        onDismiss: (() -> Unit)?,
    ) {
        logFirebase { message ?: "Failure on base view fragment" }
        hideProgress()
        val errorMessage = message ?: "Oops! An error has occurred. Please try again."
        showSnackBar(view, errorMessage, SnackBarUtils.SnackBarType.ALERT, onDismiss)
    }

    override fun showInfo(
        message: String,
        onDismiss: (() -> Unit)?,
    ) {
        showSnackBar(view, message, SnackBarUtils.SnackBarType.INFO, onDismiss)
    }

    protected fun showSuccess(
        message: String,
        onDismiss: (() -> Unit)? = null,
    ) {
        showSnackBar(view, message, SnackBarUtils.SnackBarType.SUCCESS, onDismiss)
    }

    protected open fun showProgress() {
        progressBar?.visibility = View.VISIBLE
    }

    protected open fun hideProgress() {
        progressBar?.visibility = View.GONE
    }

    protected fun hideKeyBoard() {
        (activity as BaseCommentActivity).hideSoftKeyboard()
    }

    protected fun showKeyboard(view: View) {
        (activity as BaseCommentActivity).showSoftKeyboard(view)
    }

    protected fun showKeyboardForced() {
        (activity as BaseCommentActivity).showSoftKeyboardForced()
    }

    protected fun showTitle(title: CharSequence) {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = title
    }

    protected fun hideToolbarOnScroll() {
        val params = toolbar?.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
    }

    protected fun startAccessActivity(
        activity: Activity?,
        requestCode: Int = BaseCommentActivity.LOGIN_REQUEST,
    ) {
        activity?.let { safeActivity ->
            val accessClass = baseFeaturesNavigator.getAccessActivityClass()
            val intent = Intent(safeActivity, accessClass)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            safeActivity.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        if (displayCartItem) {
            menu.clear()
        } else if (displayMoreOptions) {
            menu.clear()
        }
    }


    override fun onResume() {
        super.onResume()
        if (shouldUseDefaultTracking) {
        }
        if (displayCartItem && tvNbCartItems != null) {
        }
    }

    private fun initToolbar() {
        if (toolbar == null) {
            return
        }
        setHasOptionsMenu(true)
        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)
                setDisplayShowHomeEnabled(displayShowHomeEnabled)
                setDisplayShowTitleEnabled(displayShowTitleEnabled)
                title = null
            }
        }
    }

    protected fun overrideToolbar(
        view: View?,
        toolbarId: Int,
        toolbarTitle: String? = null,
        displayHomeAsUpEnabled: Boolean = false,
        displayShowHomeEnabled: Boolean = false,
        displayShowTitleEnabled: Boolean = false,
        hasOptionsMenu: Boolean = true,
    ) {
        toolbar = view?.findViewById(toolbarId)

        if (toolbar == null) {
            return
        }
        val navigationIconColorStateList =
            ContextCompat.getColorStateList(requireContext(), R.color.dark_black)
        setHasOptionsMenu(hasOptionsMenu)
        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)
                setDisplayShowHomeEnabled(displayShowHomeEnabled)
                setDisplayShowTitleEnabled(displayShowTitleEnabled)
                title = toolbarTitle
            }
        }
        toolbar?.navigationIcon?.setTintList(navigationIconColorStateList)
    }
}
