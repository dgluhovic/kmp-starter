package com.kmp_starter.core.base

import kotlin.native.concurrent.freeze

actual fun freeze(obj: Any) {
    obj.freeze()
}