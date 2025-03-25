package com.qy.cloud.archcore.coroutines

/**
 * This sealed class can be used by legacy calls / use cases where we are not waiting
 * the completion of the API call to update the UI.
 *
 * [PreWebserviceCallAction] can be emitted once needed checks inside the [UseCase] are done.
 * [PostWebserviceCallAction] can be emitted once the API call is done with success.
 */
sealed class WebserviceCallAction {
    object PreWebserviceCallAction : WebserviceCallAction()
    object PostWebserviceCallAction : WebserviceCallAction()
}
