package com.kmp_starter.app

import android.content.Intent
import com.kmp_starter.core.userinfo.UserInfoMode

private const val USER_INFO_MODE = "user_info_mode"

fun Intent.withUserInfoMode(mode: UserInfoMode) = putExtra(
    USER_INFO_MODE, mode)

fun Intent.userInfoMode(defaultMode: UserInfoMode = UserInfoMode.REGISTER)
        = (getSerializableExtra(USER_INFO_MODE) as? UserInfoMode) ?: defaultMode