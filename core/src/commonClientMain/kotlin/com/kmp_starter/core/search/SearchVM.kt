package com.kmp_starter.core.search

import com.kmp_starter.core.base.*
import com.kmp_starter.core.data.DEFAULT_SEARCH_RADIUS_MILES
import com.kmp_starter.core.data.UserRepo
import com.kmp_starter.data.Address
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class SearchVM(
    private val userRepo: UserRepo,
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) : BaseVM<SearchEvent, SearchResult, SearchState, SearchEffect>(dispatcher, SearchState()) {

    override fun Flow<SearchEvent>.eventToResults(): Flow<Lce<out SearchResult>> = flowOf(
        ofType<SearchEvent.ScreenLoad>().onScreenLoad(),
        ofType<SearchEvent.SearchClick>().onSearchClick()
    ).flattenMerge()

    override fun updateStateWithResult(
        currentState: SearchState,
        lce: Lce<out SearchResult>
    ): SearchState = when (lce) {
        is Lce.Content -> when (val result = lce.content) {
            is SearchResult.ScreenLoad -> currentState.copy(result.user)
            is SearchResult.SearchResponse -> currentState.copy(items = result.items).copyDistance(result.distanceMiles)
            else -> currentState //TODO
        }
        else -> currentState
    }

    override fun Flow<Lce<out SearchResult>>.resultToEffects(): Flow<SearchEffect> =
        ofType<Lce.Error<out SearchResult>>().flatMapConcat { flowOf(SearchEffect.Error(it.throwable)) }

    private fun Flow<SearchEvent.ScreenLoad>.onScreenLoad() = flatMapLatest {
        viewState.take(1)
            .flatMapLatest {
                it.me?.let { user ->
                    flowOf(SearchResult.ScreenLoad(user))
                } ?: userRepo.me()
                        .flatMapLatest { user ->
                            flowOf(
                                flowOf(SearchResult.ScreenLoad(user)),
                                searchFlow(it.distanceMiles)
                            )
                        }.flattenMerge()
            }
    }.wrapWithLce()

    private fun Flow<SearchEvent.SearchClick>.onSearchClick() =
        flatMapLatest {
            searchFlow(it.distanceMiles)
                .wrapWithLce()
        }

    private fun searchFlow(distanceMiles: Double) =
        userRepo.search(distanceMiles)
            .flatMapLatest { results ->
                viewState.take(1)
                    .flatMapLatest {
                        it.me?.let { me ->
                            flowOf(
                                SearchResult.SearchResponse(
                                    items = listOf(
                                        SearchAdapterItem.Header(distanceMiles),
                                        *results.map {
                                            SearchAdapterItem.SearchResult(it)
                                        }
                                            .sortedBy { it.result.distanceMeters }
                                            .toTypedArray()
                                    ),
                                    distanceMiles = distanceMiles
                                )
                            )
                        } ?: emptyFlow()
                    }
            }
}