package com.qy.cloud.base.scene.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.qy.cloud.archcore.coroutines.extensions.logD
import com.qy.cloud.archcore.coroutines.extensions.logV
import com.qy.cloud.archcore.coroutines.extensions.toContextWithLocaleConfiguration
import com.qy.cloud.base.R
import com.qy.cloud.base.utils.ActivityLauncher
import com.qy.cloud.base.utils.Backable
import com.qy.cloud.base.utils.GenericView
import com.qy.cloud.base.utils.SnackBarUtils
import com.qy.cloud.base.BaseFeaturesNavigator
import org.koin.android.ext.android.inject

abstract class BaseCommentActivity : AppCompatActivity(), GenericView {

    open val layoutRes = R.layout.activity_base

    open val needLogin = false

    private val baseFeaturesNavigator by inject<BaseFeaturesNavigator>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)

//        loginChecks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SESSION_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    recreate()
                } else {
                    finish()
                }
                return
            }

            LOGIN_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    recreate()
                } else {
                    if (isTaskRoot) {
                        ActivityLauncher.startHomeActivity(this)
                    }
                    finish()
                }
            }

            LOGIN_REQUEST_WITHOUT_RELOAD -> {
                if (resultCode == Activity.RESULT_OK) {
                    recreate()
                } else {
                    // do nothing
                    return
                }
            }
        }
        if (resultCode == RESULT_CODE_SHOW_SUCCESS_SNACKBAR) {
            val successMsg = data?.getStringExtra(SUCCESS_MESSAGE_SNACKBAR)
            successMsg?.let { showSuccess(it) }
        }
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context.toContextWithLocaleConfiguration())
    }

    /**
     * Generic method to replace main fragment
     *
     * @param tag            tag
     * @param addToBackStack (true/false)
     * @param fragment       we want to show
     */
    open fun setFragmentInMainContainer(
        tag: String,
        addToBackStack: Boolean = false,
        fragment: () -> Fragment,
    ) {
        if (tag.isNotEmpty() &&
            supportFragmentManager.findFragmentByTag(tag) != null
        ) {
            return
        }
        loginChecks(fragment(), addToBackStack, tag)
    }

    open fun setFragmentInMainContainer(
        fragment: Fragment,
        addToBackStack: Boolean,
        tag: String,
    ) {
        logV { "setFragmentInMainContainer, tag : [$tag]" }
        loginChecks(fragment, addToBackStack, tag)
    }

    open fun replaceFragmentInMainContainer(
        fragment: Fragment,
        addToBackStack: Boolean,
        tag: String,
        cleanStack: Boolean = false,
    ) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        fragmentTransaction.replace(R.id.base_frame_layout, fragment, tag)

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(tag)
        }

        if (cleanStack){
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        fragmentTransaction.commit()
    }

    fun hideSoftKeyboard() {
        currentFocus?.let {
            hideSoftKeyboard(it)
        }
    }

    fun hideSoftKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            view.postDelayed({ imm.showSoftInput(view, 0) }, 100)
        }
    }

    fun showSoftKeyboardForced() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    override fun showError(
        message: String?,
        onDismiss: (() -> Unit)?,
    ) {
        showSnackBar(
            window.decorView.rootView,
            message ?: "Oops! An error has occurred. Please try again.",
            SnackBarUtils.SnackBarType.ALERT,
            onDismiss,
        )
    }

    protected fun showSuccess(
        message: String,
        onDismiss: (() -> Unit)? = null,
    ) {
        val view = findViewById<FrameLayout>(R.id.base_frame_layout)
        showSnackBar(view, message, SnackBarUtils.SnackBarType.SUCCESS, onDismiss)
    }

    override fun showInfo(
        message: String,
        onDismiss: (() -> Unit)?,
    ) {
        showSnackBar(window.decorView.rootView, message, SnackBarUtils.SnackBarType.INFO, onDismiss)
    }

    protected fun showSnackBar(
        snackBarLayout: View,
        message: String,
        snackBarType: SnackBarUtils.SnackBarType,
        onDismiss: (() -> Unit)?,
    ) {
        SnackBarUtils.showSnackBar(this, snackBarLayout, message, snackBarType, onDismiss)
    }

    fun pushFakePopup(f: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            addToBackStack("fakePopup")
            add(R.id.base_frame_layout, f, "fakePopup")
            commitAllowingStateLoss()
        }
    }

    fun showTitle(title: CharSequence) {
        supportActionBar?.title = title
    }

    override fun onBackPressed() {
        supportFragmentManager.let { fm ->
            if (fm.backStackEntryCount > 0) {
                val lastFragmentName = fm.getBackStackEntryAt(fm.backStackEntryCount - 1).name
                val currentFragment = fm.findFragmentByTag(lastFragmentName)
                if (currentFragment != null && currentFragment is Backable) {
                    if ((currentFragment as Backable).onBackPressed()) {
                        return
                    } else {
                        super.onBackPressed()
                        return
                    }
                }
            }
            for (fragment in fm.fragments) {
                if (fragment is Backable && fragment.onBackPressed()) {
                    return
                }
            }
        }
        if (isTaskRoot) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }

    private fun loginChecks(
        fragment: Fragment? = null,
        addToBackStack: Boolean = false,
        tag: String? = null,
    ) {
        logD { "SessionLog - [$localClassName] - loginChecks" }
        if (needLogin) {
            startAccessActivity()
        } else {
            fragment?.let { setFragment(it, addToBackStack, tag) }
        }
    }

    private fun startAccessActivity() {
        logD { "SessionLog - [$localClassName] - Show AccessActivity" }
        //todo goto login
        val accessClass = baseFeaturesNavigator.getAccessActivityClass()
        val intent = Intent(this, accessClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivityForResult(intent, LOGIN_REQUEST)
    }

    private fun setFragment(
        fragment: Fragment,
        addToBackStack: Boolean,
        tag: String?,
    ) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out,
        )
        fragmentTransaction.add(
            R.id.base_frame_layout,
            fragment,
            tag,
        ) // Todo replace by android.R.id.content
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(tag)
        }
        fragmentTransaction.commitAllowingStateLoss()
    }

    companion object {
        const val RESULT_CODE_SHOW_SUCCESS_SNACKBAR = 383568
        const val SUCCESS_MESSAGE_SNACKBAR = "SUCCESS_SNACKBAR_MSG"

        const val SESSION_REQUEST = 2894
        const val LOGIN_REQUEST = 2895
        const val LOGIN_REQUEST_WITHOUT_RELOAD = 2896
        private const val RESTORE_SESSION = "RESTORE_SESSION"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
