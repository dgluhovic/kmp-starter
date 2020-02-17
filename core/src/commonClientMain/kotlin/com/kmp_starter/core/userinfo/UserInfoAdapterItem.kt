package com.kmp_starter.core.userinfo

import com.kmp_starter.app.com.kmp_starter.core.base.InputType
import com.kmp_starter.core.MR
import com.kmp_starter.data.Address
import dev.icerock.moko.resources.desc.StringDesc

enum class UserInfoAdapterItemType {
    INPUT,
    AUTOCOMPLETE
}

abstract sealed class UserInfoAdapterItem(
    val text: String,
    val hint: StringDesc,
    val inputType: InputType = InputType.TEXT_CAP_WORDS,
    val type: UserInfoAdapterItemType = UserInfoAdapterItemType.INPUT
) {
    open val valid: Boolean = text.isNotEmpty()

    data class UserName(val name: String = "") : UserInfoAdapterItem(
        name,
        StringDesc.Resource(MR.strings.name)
    )

    data class UserEmail(val name: String = "") : UserInfoAdapterItem(
        name,
        StringDesc.Resource(MR.strings.email),
        InputType.TEXT_EMAIL
    ) {
        override val valid: Boolean
            get() = name.isValidEmail()
    }

    data class UserPassword(val name: String = "") : UserInfoAdapterItem(
        name,
        StringDesc.Resource(MR.strings.password),
        InputType.TEXT_PASSWORD
    ) {
        override val valid: Boolean
            get() = name.length > 5 //TODO
    }

    data class UserAddress(
        val name: String = "",
        val address: Address? = null,
        val addressSuggestions: List<String> = emptyList()
    ) : UserInfoAdapterItem(
        name,
        StringDesc.Resource(MR.strings.address_hint),
        type = UserInfoAdapterItemType.AUTOCOMPLETE
    ) {
        override val valid: Boolean
            get() = address != null //TODO
    }
}

private const val EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$"
private fun String.isValidEmail(): Boolean = Regex(EMAIL_REGEX, RegexOption.IGNORE_CASE).matches(this)
