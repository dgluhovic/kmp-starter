package com.kmp_starter.core.vmfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kmp_starter.core.data.UserRepo
import com.kmp_starter.core.search.SearchVM

class SearchVMFactory(
    private val userRepo: UserRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = SearchVM(userRepo) as T
}