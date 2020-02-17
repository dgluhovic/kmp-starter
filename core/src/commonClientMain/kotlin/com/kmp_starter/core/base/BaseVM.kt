package com.kmp_starter.core.base


import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.Logger
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

abstract class BaseVM<EVENT, RESULT, STATE, EFFECT>(
    private val dispatcher: CoroutineDispatcher,
    private val initialState: STATE
) : VM(), CoroutineScope {

    private val channel = BroadcastChannel<EVENT>(Channel.BUFFERED)
    private val stateChannel = ConflatedBroadcastChannel<Pair<Lce<out RESULT>, STATE>>()

    val viewState: Flow<STATE>
    val viewEffects: Flow<EFFECT>
    val job: Job

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + dispatcher

    init {
        viewState = stateChannel
            .asFlow()
            .map { it.second }
            .distinctUntilChanged()

        viewEffects = stateChannel
            .asFlow()
            .map { it.first }
            .resultToEffects()

        job = launch {
            channel
                .asFlow()
                .eventToResults()
                .resultsWithState()
                .collect {
                    stateChannel.send(it)
                }
        }
    }

    abstract fun updateStateWithResult(currentState: STATE, lce: Lce<out RESULT>): STATE
    abstract fun Flow<EVENT>.eventToResults(): Flow<Lce<out RESULT>>
    abstract fun Flow<Lce<out RESULT>>.resultToEffects(): Flow<EFFECT>

    private fun Flow<Lce<out RESULT>>.resultsWithState(): Flow<Pair<Lce<out RESULT>, STATE>> = scan(Pair(
        Lce.Loading(), initialState)) {
            pair: Pair<Lce<out RESULT>, STATE>, lce: Lce<out RESULT> ->
        Pair(lce, updateStateWithResult(pair.second, lce))
    }


    fun processInput(event: EVENT) {
        launch {
            channel.send(event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

inline fun <reified T> Flow<*>.ofType(): Flow<T> = filter { it is T }.map { it as T }

fun <T> Flow<T>.wrapWithLce(emitLoading: Boolean = true): Flow<Lce<T>> = map { Lce.Content(it) as Lce<T> }
    .onStart { if (emitLoading) emit(Lce.Loading<T>()) }
    .catch { emit(Lce.Error(it)) }

fun Throwable?.isConflictApiError() =
    (this as? ClientRequestException)?.response?.status == HttpStatusCode.Conflict

sealed class Lce<T> {
    data class Error<T>(val throwable: Throwable?) : Lce<T>()
    class Loading<T> : Lce<T>()
    data class Content<T>(val content: T) : Lce<T>()
}

expect abstract class VM() {
    open fun onDestroy()
}