package com.qy.cloud.archcore.coroutines.wrappers

/**
 * This a base class serving as wrappers for ActivityResultLaunchers
 *
 * This will be used by feature API modules to share an ActivityResultLauncher reference.
 * Since our API modules are plain Kotlin modules and we don't want it to have Android
 * dependencies.
 * These classes should only be used as wrappers for arguments for an API/method to pass
 * Activities but should never be used to store any of the activity reference.
 * Please Note: This class should not be used to share context, direct injection via Koin will
 * always be preferable.
 * Concrete implementations of the class can be found in the :base module
 */
interface ActivityResultLauncherWrapper
