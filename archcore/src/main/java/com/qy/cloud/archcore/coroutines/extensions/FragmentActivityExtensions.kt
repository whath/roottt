package com.qy.cloud.archcore.coroutines.extensions

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

/**
 * [FragmentActivity] extension to replace the fragment in the [containerViewId].
 * If the fragment already exists in the back stack it will call [FragmentManager.popBackStack]
 * in order to reuse the Fragment and avoid navigation loop. Otherwise a new instance of this
 * Fragment is created and added in the back stack.
 *
 * @param containerViewId Identifier of the container whose fragment(s) are to be replaced.
 * @param tag Fragment's tag used to retrieve the fragment and add it as name into the back stack.
 * @param flags Can be 0 (default value) or [FragmentManager.POP_BACK_STACK_INCLUSIVE].
 * @param getFragmentByTag Lambda to execute to get the fragment from the tag name.
 */
fun FragmentActivity.replaceFragment(
    @IdRes containerViewId: Int,
    tag: String,
    flags: Int = 0,
    getFragmentByTag: (String) -> Fragment?,
) {
    val fragmentManager: FragmentManager = supportFragmentManager
    val fragment: Fragment? = fragmentManager.findFragmentByTag(tag)
    if (fragment != null) {
        fragmentManager.popBackStack(tag, flags)
    } else {
        getFragmentByTag(tag)?.let {
            fragmentManager.beginTransaction()
                .replace(containerViewId, it, tag)
                .addToBackStack(tag)
                .commit()
        }
    }
}

fun FragmentActivity.replaceFragmentWithAnimation(
    @IdRes containerViewId: Int,
    tag: String,
    flags: Int = 0,
    getFragmentByTag: (String) -> Fragment?,
) {
    val fragmentManager: FragmentManager = supportFragmentManager
    val fragment: Fragment? = fragmentManager.findFragmentByTag(tag)
    if (fragment != null) {
        fragmentManager.popBackStack(tag, flags)
    } else {
        getFragmentByTag(tag)?.let {
            fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(containerViewId, it, tag)
                .addToBackStack(tag)
                .commit()
        }
    }
}
