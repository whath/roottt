package com.qy.cloud.archcore.coroutines.extensions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Enum<T>> Intent.putExtra(victim: T): Intent =
    putExtra(T::class.qualifiedName, victim.ordinal)

inline fun <reified T : Enum<T>> Intent.getEnumExtra(): T? =
    getIntExtra(T::class.qualifiedName, -1)
        .takeUnless { it == -1 }
        ?.let { T::class.java.enumConstants?.get(it) }

/**
 * An extension that handles extracting a [Serializable] object from an Intent.
 * It's an inline function to be used inside kotlin classes.
 *
 * Deprecation warnings are suppressed as the original method is still to be used for build versions
 * before Tiramisu (33).
 *
 * @param key The chosen key for the Serializable to extract
 * @return a Serializable value, or null
 */
@Suppress("deprecation")
inline fun <reified T : Serializable> Intent.getSerializableExtraSafe(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, T::class.java)
    } else {
        getSerializableExtra(key) as? T?
    }
}

/**
 * An extension that handles extracting a [Serializable] object from an Intent.
 * It's a function to be used inside Java classes.
 *
 * Deprecation warnings are suppressed as the original method is still to be used for build versions
 * before Tiramisu (33). Also for the unchecked cast, as still the type T should be [Serializable].
 *
 * @param key The chosen key for the Serializable to extract
 * @param clazz The expected class of the returned type
 * @return a Serializable value, or null
 */
@Suppress("deprecation", "UNCHECKED_CAST")
fun <T : Serializable?> Intent.getSerializableExtraSafeJava(
    key: String,
    clazz: Class<T>,
): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, clazz)
    } else {
        getSerializableExtra(key) as? T?
    }
}

/**
 * An extension that handles extracting a [Parcelable] object from a Bundle.
 * It's an inline function to be used inside kotlin classes.
 *
 * Deprecation warnings are suppressed as the original method is still to be used for build versions
 * before Tiramisu (33).
 *
 * @param key The chosen key for the Parcelable to extract
 * @return a getParcelable value, or null
 */
inline fun <reified T : Parcelable?> Intent.getParcelableExtraSafe(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("deprecation")
        getParcelableExtra(key)
    }
}

/**
 * An extension that handles extracting an ArrayList<Parcelable> from a Bundle.
 * It's an inline function to be used inside kotlin classes.
 *
 * Deprecation warnings are suppressed as the original method is still to be used for build versions
 * before Tiramisu (33).
 *
 * @param key The chosen key for the ArrayList<Parcelable> to extract
 * @return a getParcelable value, or null
 */
@Suppress("unused")
inline fun <reified T : Parcelable?> Intent.getParcelableArrayListSafe(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, T::class.java)
    } else {
        @Suppress("deprecation")
        getParcelableArrayListExtra(key)
    }
}

/**
 * An extension that handles extracting a [Parcelable] object from a Bundle.
 * It's an inline function to be used inside Java classes.
 *
 * Deprecation warnings are suppressed as the original method is still to be used for build versions
 * before Tiramisu (33).
 *
 * @param key The chosen key for the Parcelable to extract
 * @return a getParcelable value, or null
 */
fun <T : Parcelable?> Intent.getParcelableExtraSafeJava(
    key: String,
    clazz: Class<T>,
): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, clazz)
    } else {
        @Suppress("deprecation")
        getParcelableExtra(key)
            as? T?
    }
}

inline fun <reified T : android.app.Activity> Context.intentTo(options: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java).apply(options)
    startActivity(intent)
}
