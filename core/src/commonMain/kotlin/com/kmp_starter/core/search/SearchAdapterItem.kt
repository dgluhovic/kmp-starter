package com.kmp_starter.core.search

import com.kmp_starter.data.User

sealed class SearchAdapterItem {
    object Header : SearchAdapterItem()
    data class SearchResult(
        val user: User,
        val distanceMiles: Double
    ) : SearchAdapterItem()
}
