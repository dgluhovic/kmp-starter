package com.kmp_starter.core.base

import platform.Foundation.NSLog

actual fun debug(tag: String?, text: String) {
    NSLog(text)
}

actual fun error(tag: String?, text: String) {
    NSLog(text)
}

actual fun warn(tag: String?, text: String) {
    NSLog(text)
}