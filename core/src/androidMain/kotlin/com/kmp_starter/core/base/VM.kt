package com.kmp_starter.core.base

import androidx.lifecycle.ViewModel

actual abstract class VM : ViewModel() {
    actual open fun onDestroy() {
        onCleared()
    }
}