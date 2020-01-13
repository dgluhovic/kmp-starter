package com.kmp_starter.core.base

internal sealed class Event {
    internal data class A(val value: Int) : Event()
    internal data class B(val value: Boolean) : Event()
    internal data class Delayed(val random: Long) : Event()
    internal object Submit : Event()
}

internal sealed class Result {
    internal data class A(val value: Int) : Result()
    internal data class B(val value: Boolean) : Result()
    internal data class Delayed(val random: Long) : Result()
    internal data class Submit(val random: Long?) : Result()
}

internal object Effect
internal data class State(val text: String = "", val loading: Boolean = false, val random: Long? = null)