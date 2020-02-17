package com.kmp_starter.core.base

import io.ktor.util.date.GMTDate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.test.*

abstract class BaseVMTest(private val sleeper: Sleeper) : CoroutineScope {

    internal val fakeInitialState: State = State()

    internal val viewModel by lazy { TestVM(fakeInitialState, Dispatchers.Unconfined) }
    internal val flatViewModel by lazy { FlatMapTestVM(fakeInitialState, Dispatchers.Unconfined) }

    override val coroutineContext: CoroutineContext =
        Dispatchers.Unconfined

    @Test
    fun shouldEmitInitialState() {

        val observer = viewModel.viewState.test(this, sleeper)
        val observerFlat = flatViewModel.viewState.test(this, sleeper)

        observer.awaitCount(1)
        observerFlat.awaitCount(1)

        observer.assertValueAt(0, fakeInitialState)
        observerFlat.assertValueAt(0, fakeInitialState)

        observer.dispose()
        observerFlat.dispose()
    }

    @Test
    fun shouldEmitStatesWithExpectedValue_whenReceivingEvent() {
        val observer = viewModel.viewState.test(this, sleeper)
        val observerFlat = flatViewModel.viewState.test(this, sleeper)
        sendEvents(times = 2)

        observer.awaitCount(2)
        observerFlat.awaitCount(2)
        (1..2).forEach { i ->
            observer.assertValueAt(i) { it.text == "$i" }
            observerFlat.assertValueAt(i) { it.text == "$i" }
        }
    }

    private fun sendEvents(times: Int) {
        (1..times).forEach { viewModel.processInput(Event.A(it)) }
        (1..times).forEach { flatViewModel.processInput(Event.A(it)) }
    }

    @Test
    fun shouldCacheStateWithExpectedValue_whenReceivingEventBeforeSubscription() {
        sendEvents(times = 2)

        val observer = viewModel.viewState.test(this, sleeper)
        val observerFlat = flatViewModel.viewState.test(this, sleeper)

        observer.awaitCount(1)
        observerFlat.awaitCount(1)

        observer.assertValueAt(0, State(text = "2"))
        observerFlat.assertValueAt(0, State(text = "2"))
    }

    @Test
    fun shouldCacheLatestState_whenUnsubscribingThenResubscribing() {
        var observer = viewModel.viewState.test(this, sleeper)
        var observerFlat = flatViewModel.viewState.test(this, sleeper)
        sendEvents(times = 4)
        observer.dispose()
        observerFlat.dispose()

        observer = viewModel.viewState.test(this, sleeper)
        observerFlat = viewModel.viewState.test(this, sleeper)

        observer.awaitCount(1)
        observerFlat.awaitCount(1)

        observer.assertValueAt(0) { it.text == "4" }
        observerFlat.assertValueAt(0) { it.text == "4" }
    }

    @Test
    fun shouldEmitEffect_whenReceivingEvent() {
        val observer = viewModel.viewEffects.test(this,  sleeper)
        val observerFlat = flatViewModel.viewEffects.test(this, sleeper)
        sendEvents(times = 6)

        observer.awaitCount(2)
        observerFlat.awaitCount(2)

        observer.assertValueAt(0, Effect)
        observer.assertValueAt(1, Effect)
        observerFlat.assertValueAt(0, Effect)
        observerFlat.assertValueAt(1, Effect)
    }

    @Test
    fun shouldEmitEffectAndState_whenReceivingEventB() {
        val observerState = viewModel.viewState.test(this, sleeper)
        val observerEffect = viewModel.viewEffects.test(this, sleeper)
        val observerFlatState = flatViewModel.viewState.test(this, sleeper)
        val observerFlatEffect = flatViewModel.viewEffects.test(this, sleeper)

        viewModel.processInput(Event.B(true))
        flatViewModel.processInput(Event.B(true))

        observerState.awaitCount(2)
        observerFlatState.awaitCount(2)
        observerState.assertValueAt(0, State())
        observerState.assertValueAt(1, State(loading = true))
        observerFlatState.assertValueAt(0, State())
        observerFlatState.assertValueAt(1, State(loading = true))

        observerEffect.awaitCount(1)
        observerFlatEffect.awaitCount(1)
        observerEffect.assertValueAt(0, Effect)
        observerFlatEffect.assertValueAt(0, Effect)
    }

    @Test
    fun shouldSwitchOnDelayedEvent() {
        val observerFlat = flatViewModel.viewState.test(this, sleeper)

        // Fire 5 events, we should only see 1 result
        flatViewModel.processInput(Event.Delayed(1))
        flatViewModel.processInput(Event.A(2))
        flatViewModel.processInput(Event.Delayed(2))
        flatViewModel.processInput(Event.Delayed(3))
        flatViewModel.processInput(Event.Delayed(4))
        flatViewModel.processInput(Event.Delayed(5))

        observerFlat.awaitCount(3)
        observerFlat.assertValueAt(0, State())
        observerFlat.assertValueAt(1, State(text = "2"))
        observerFlat.assertValueAt(2, State(text = "2", random = 5))
    }

    internal class TestVM(
        state: State,
        dispatcher: CoroutineDispatcher
    ) : BaseVM<Event, Result, State, Effect>(dispatcher, state) {

        override fun Flow<Event>.eventToResults(): Flow<Lce<out Result>> {
            return flowOf(
                ofType<Event.A>().map {
                    Lce.Content(
                        Result.A(it.value)
                    )
                },
                ofType<Event.B>().map {
                    Lce.Content(
                        Result.B(it.value)
                    )
                }
            ).flattenMerge()
        }

        override fun updateStateWithResult(currentState: State, lce: Lce<out Result>): State {
            return when (lce) {
                is Lce.Content -> when (val result = lce.content) {
                    is Result.A -> currentState.copy(text = "${result.value}")
                    is Result.B -> currentState.copy(loading = result.value)
                    else -> currentState
                }
                else -> currentState
            }
        }

        override fun Flow<Lce<out Result>>.resultToEffects(): Flow<Effect> {
            return flatMapConcat {
                when (it) {
                    is Lce.Content -> {
                        val result = it.content
                        when {
                            result is Result.A && result.value % 3 == 0 -> flowOf(
                                Effect
                            )
                            result is Result.B && result.value -> flowOf(
                                Effect
                            )
                            else -> emptyFlow()
                        }
                    }
                    else -> emptyFlow()
                }
            }
        }
    }

    internal class FlatMapTestVM(state: State, dispatcher: CoroutineDispatcher) :
        BaseVM<Event, Result, State, Effect>(dispatcher, state) {

        override fun Flow<Event>.eventToResults(): Flow<Lce<out Result>> {
            val switch = flatMapLatest { event ->
                when (event) {
                    is Event.Delayed -> flowOf(
                        Lce.Content(Result.Delayed(event.random))
                    )
                        .onStart { delay(200) }
                    else -> emptyFlow()
                }
            }
            val map = flatMapConcat { event ->
                when (event) {
                    is Event.A -> flowOf(
                        Lce.Content(Result.A(event.value))
                    )
                    is Event.B -> flowOf(
                        Lce.Content(Result.B(event.value))
                    )
                    else -> emptyFlow()
                }
            }
            return flowOf(switch, map).flattenMerge()
        }

        override fun updateStateWithResult(currentState: State, lce: Lce<out Result>): State {
            return when (lce) {
                is Lce.Content -> when (val result = lce.content) {
                    is Result.A -> currentState.copy(text = "${result.value}")
                    is Result.B -> currentState.copy(loading = result.value)
                    is Result.Delayed -> currentState.copy(random = result.random)
                    else -> currentState
                }
                else -> currentState
            }
        }

        override fun Flow<Lce<out Result>>.resultToEffects(): Flow<Effect> = flatMapConcat {
            when (it) {
                is Lce.Content -> {
                    val result = it.content
                    when {
                        result is Result.A && result.value % 3 == 0 -> flowOf(
                            Effect
                        )
                        result is Result.B && result.value -> flowOf(
                            Effect
                        )
                        else -> emptyFlow()
                    }
                }
                else -> emptyFlow()
            }
        }
    }

    internal class SubOnInitTestVM(
        private val batchName: String,
        dispatcher: CoroutineDispatcher
    ) : BaseVM<Event, Result, State, Nothing>(
        initialState = State(text = batchName), dispatcher = dispatcher
    ) {
        override fun Flow<Event>.eventToResults(): Flow<Lce<out Result>> {
            return flowOf(
                ofType<Event.A>().map {
                    Lce.Content(
                        Result.A(it.value)
                    )
                },
                ofType<Event.B>().map {
                    Lce.Content(
                        Result.B(it.value)
                    )
                }
            ).flattenMerge()
        }

        override fun updateStateWithResult(currentState: State, lce: Lce<out Result>): State {
            return when (lce) {
                is Lce.Content -> when (val result = lce.content) {
                    is Result.A -> currentState.copy(text = "${result.value}")
                    is Result.B -> currentState.copy(loading = result.value)
                    else -> currentState
                }
                else -> currentState
            }
        }

        override fun Flow<Lce<out Result>>.resultToEffects(): Flow<Nothing> =
            emptyFlow()
    }

    internal class TestObserver<T>(
        private val scope: CoroutineScope,
        private val flow: Flow<T>,
        private val sleeper: Sleeper
    ) {
        private val values = mutableListOf<T>()
        private val job: Job

        init {
            job = scope.launch { flow.collect { values.add(it) } }
        }

        fun awaitCount(count: Int, timeout: Long = 2000) {
            val start = GMTDate().timestamp
            println("timestamp start   $start")
            while (true) {
                if (values.count() >= count) {
                    println("timestamp success $values")
                    break
                }

                if (GMTDate().timestamp > start + timeout) {
                    println("timestamp timeout ${GMTDate().timestamp}")
                    break
                }

                sleeper.sleepThread(10)
            }
        }

        fun assertValueAt(i: Int, fakeInitialState: T) {
            assertEquals(fakeInitialState, values[i])
        }

        fun assertValueAt(index: Int, check: (T) -> Boolean) {
            assertTrue { check.invoke(values[index]) }
        }

        fun dispose() = job.cancel()
    }

    internal fun <T> Flow<T>.test(
        scope: CoroutineScope,
        sleeper: Sleeper
    ): TestObserver<T> = TestObserver(scope, this, sleeper)
}