package com.qy.cloud.archcore.coroutines.extensions

/**
 * Checks if all objects passed as argument is the same type as [T]
 *
 * @param T the type to compare.
 * @param objects objects to be checked
 * @return `true` if all objects are the same type as [T], `false` otherwise.
 */
inline fun <reified T> allInstanceOf(vararg objects: Any) =
    objects.all { item -> item is T }

/**
 * Checks if all objects passed as argument are not null
 *
 * @param T the type to compare.
 * @param objects objects to be checked
 * @param block the block with the same elements to run if all of them are not null.
 */
inline fun <T : Any> ifAllNotNull(vararg objects: T?, block: (List<T>) -> Unit) {
    if (objects.all { it != null }) {
        block(objects.filterNotNull())
    }
}

/**
 * Checks if one object in the list is null. In case one elem is null, return null else return
 * the list
 *
 * @param T the type to compare.
 * @param objects objects to be checked
 */
fun <T : Any> allNotNullElseNull(vararg objects: T?): List<T>? =
    if (objects.contains(null)) {
        null
    } else {
        objects.filterNotNull()
    }

/**
 * Returns the first element that is an instance of specified type parameter R, or null if none
 * are present
 */
inline fun <reified R> Iterable<*>.firstInstanceOrNull(): R? =
    filterIsInstance<R>().firstOrNull()