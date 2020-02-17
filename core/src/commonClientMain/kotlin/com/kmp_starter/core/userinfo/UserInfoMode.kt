package com.kmp_starter.core.userinfo

import com.kmp_starter.core.MR
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc

enum class UserInfoMode(
    titleId: StringResource,
    primaryButtonId: StringResource,
    secondaryButtonId: StringResource
)
{
    REGISTER(MR.strings.register, MR.strings.register, MR.strings.login),
    LOGIN(MR.strings.login, MR.strings.login, MR.strings.forgot_password),
    PROFILE(MR.strings.profile, MR.strings.save, MR.strings.reset);

    val title: StringDesc
    val primaryButton: StringDesc
    val secondaryButton: StringDesc

    init {
        title = StringDesc.Resource(titleId)
        primaryButton = StringDesc.Resource(primaryButtonId)
        secondaryButton = StringDesc.Resource(secondaryButtonId)
    }
}