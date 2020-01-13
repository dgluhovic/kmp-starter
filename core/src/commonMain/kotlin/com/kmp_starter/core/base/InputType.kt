package com.kmp_starter.app.com.kmp_starter.core.base

expect enum class InputType {
    TEXT_CAP_WORDS,
    TEXT_PASSWORD,
    TEXT_EMAIL;

    val type: Int
}