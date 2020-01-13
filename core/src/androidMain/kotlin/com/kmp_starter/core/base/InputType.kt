package com.kmp_starter.app.com.kmp_starter.core.base

actual enum class InputType(actual val type: Int) {
    TEXT_CAP_WORDS(android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS),
    TEXT_PASSWORD(android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD),
    TEXT_EMAIL(android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
}