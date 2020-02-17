package com.kmp_starter.core.search

import com.kmp_starter.core.data.DEFAULT_SEARCH_RADIUS_MILES
import com.kmp_starter.data.User

sealed class SearchAdapterItem {
    data class Header(val distanceMiles: Double = DEFAULT_SEARCH_RADIUS_MILES) : SearchAdapterItem()
    data class SearchResult(
        val result: com.kmp_starter.data.SearchResult
    ) : SearchAdapterItem()
}
