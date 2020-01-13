package com.kmp_starter.core.search

import com.kmp_starter.core.MR
import com.kmp_starter.data.Address
import com.kmp_starter.data.User
import com.kmp_starter.data.UserRegistration
import dev.icerock.moko.resources.desc.StringDesc


data class SearchState(
    val me: User? = null,
    val items: List<SearchAdapterItem> = listOf(SearchAdapterItem.Header),
    val title: StringDesc = StringDesc.Resource(MR.strings.search)
) {
}

sealed class SearchEvent {
    object ScreenLoad : SearchEvent()
    data class SearchClick(val distanceMiles: Double) : SearchEvent()
}

sealed class SearchResult {
    data class ScreenLoad(val user: User) : SearchResult()
    data class SearchResponse(val items: List<SearchAdapterItem>) : SearchResult()
    object PrimaryButtonClick : SearchResult()
}

sealed class SearchEffect {
    data class Error(val throwable: Throwable?) : SearchEffect()
}
