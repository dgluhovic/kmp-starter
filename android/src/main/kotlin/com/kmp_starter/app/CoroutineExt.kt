package com.kmp_starter.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal fun <T> CoroutineScope.launchAndCollect(flow: Flow<T>, callback: (T) -> Unit) = launch {
    flow
        .collect {
            callback(it)
        }
}