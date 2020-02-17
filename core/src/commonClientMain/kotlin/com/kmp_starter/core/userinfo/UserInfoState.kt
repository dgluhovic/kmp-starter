package com.kmp_starter.core.userinfo

import com.kmp_starter.core.MR
import com.kmp_starter.data.Address
import com.kmp_starter.data.UserRegistration
import dev.icerock.moko.resources.desc.StringDesc


data class UserInfoState(
    val mode: UserInfoMode,
    val loading: Boolean = true,
    val items: List<UserInfoAdapterItem> = emptyList(),
    val title: StringDesc = StringDesc.Resource(MR.strings.register),
    val primaryButton: StringDesc = StringDesc.Resource(MR.strings.register),
    val secondaryButton: StringDesc = StringDesc.Resource(MR.strings.login)
) {
    val primaryButtonEnabled = items.all { it.valid }

    fun userRegistration(address: Address?): UserRegistration? {
        return if (address == null || name == null || email == null || password == null) {
            null
        } else {
            UserRegistration(
                name = name!!,
                email = email!!,
                password = password!!,
                address = address
            )
        }
    }

    val name: String? get() = items.firstOrNull { it is UserInfoAdapterItem.UserName }?.text
    val email: String? get() = items.firstOrNull { it is UserInfoAdapterItem.UserEmail }?.text
    val password: String? get() = items.firstOrNull { it is UserInfoAdapterItem.UserPassword }?.text
    val address: Address? get() = (items.firstOrNull { it is UserInfoAdapterItem.UserAddress }
            as? UserInfoAdapterItem.UserAddress)?.address
}

sealed class UserInfoEvent {
    object ScreenLoad : UserInfoEvent()
    data class TextChange(val text: String, val item: UserInfoAdapterItem) : UserInfoEvent()
    data class FocusLost(val text: String, val item: UserInfoAdapterItem) : UserInfoEvent()
    object PrimaryButtonClick : UserInfoEvent()
    object SecondaryButtonClick : UserInfoEvent()
}

sealed class UserInfoResult {
    data class ScreenLoad(val items: List<UserInfoAdapterItem>) : UserInfoResult()
    data class TextChange(val text: String, val item: UserInfoAdapterItem) : UserInfoResult()
    data class FocusLost(val text: String, val item: UserInfoAdapterItem) : UserInfoResult()
    data class Geocoded(val addressSuggestions: List<Address> = emptyList()) : UserInfoResult()
    object PrimaryButtonClick : UserInfoResult()
    object StartLogin : UserInfoResult()
}

sealed class UserInfoEffect {
    data class Error(val throwable: Throwable?) : UserInfoEffect()
    object ConflictError : UserInfoEffect()
    object StartLogin : UserInfoEffect()
    object ShowHome : UserInfoEffect()
}

