package com.kmp_starter.core.vmfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kmp_starter.core.data.UserRepo
import com.kmp_starter.core.base.Geocoder
import com.kmp_starter.core.userinfo.UserInfoMode
import com.kmp_starter.core.userinfo.UserInfoVM

class UserInfoVMFactory(
    private val mode: UserInfoMode,
    private val userRepo: UserRepo,
    private val geocoder: Geocoder
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserInfoVM(
            mode,
            userRepo,
            geocoder
        ) as T
    }
}