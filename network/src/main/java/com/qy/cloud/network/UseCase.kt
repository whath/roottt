package com.qy.cloud.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import com.qy.cloud.archcore.coroutines.Result

/**
 * Use case abstraction using [Flow] to act as a contract between UI and Business.
 *
 * @param P The parameters type.
 * @param R The return type.
 *
 * @property dispatcher A [DispatcherProvider] to [Flow] executed (or [Dispatchers.IO] by
 * default).
 *
 * @see [Architecture document](https://gitlab.vestiairecollective
 * .com/Mobile/Android/AndroidVestiaire/blob/develop/guidelines/ARCH.md)
 */
abstract class UseCase<in P : Any?, out R>(
    private var dispatcher: DispatcherProvider,
) {

    /**
     * Provides to the ViewModel a single call to start the UseCase.
     * This function calls the Repository, applies any business logic required and return the
     * [Result] protected by the [Dispatchers] provided (or [Dispatchers.IO] by default).
     * Also, this call is protected against [Exception]. If receive one, return a [Result.Error].
     *
     * @param parameters The parameters needed to start the UseCase.
     *
     * @return A [Flow] of type [Result] of [R].
     */
    fun start(parameters: P? = null): Flow<Result<R>> {
        return execute(parameters)
            .catch { e -> emit(Result.Error(Exception(e))) }
            .flowOn(dispatcher.io())
    }

    /**
     * Provides to the Repository (or business layer) a call to start the UseCase.
     *
     * @param parameters The parameters needed to start the UseCase.
     *
     * @return A [Flow] of type [R].
     */
    abstract fun execute(parameters: P?): Flow<Result<R>>
}
