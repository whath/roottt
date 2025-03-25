package com.qy.cloud.archcore.coroutines.extensions

/**
 * Checks if all String objects passed as argument are not null or empty
 *
 * @param objects String objects to be checked
 * @param block the block with the same elements to run if all of them are not null or empty
 */
inline fun ifAllNotNullOrEmpty(vararg objects: String?, block: (List<String>) -> Unit) {
    if (objects.all { !it.isNullOrEmpty() }) {
        block(objects.filterNotNull())
    }
}
