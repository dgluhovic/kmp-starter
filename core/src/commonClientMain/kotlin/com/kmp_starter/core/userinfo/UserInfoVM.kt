package com.kmp_starter.core.userinfo

import com.kmp_starter.core.base.*
import com.kmp_starter.core.data.UserRepo
import com.kmp_starter.data.Address
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class UserInfoVM(
    private val mode: UserInfoMode,
    private val userRepo: UserRepo,
    private val geocoder: Geocoder,
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) : BaseVM<UserInfoEvent, UserInfoResult, UserInfoState, UserInfoEffect>(
    dispatcher, UserInfoState(mode)
) {

    override fun Flow<UserInfoEvent>.eventToResults(): Flow<Lce<out UserInfoResult>> = flowOf(
        ofType<UserInfoEvent.ScreenLoad>().onScreenLoad(),
        ofType<UserInfoEvent.TextChange>().onTextChange(),
        ofType<UserInfoEvent.FocusLost>().onFocusLost(),
        ofType<UserInfoEvent.PrimaryButtonClick>().onPrimaryButtonClick(),
        ofType<UserInfoEvent.SecondaryButtonClick>().onSecondaryButtonClick()
    ).flattenMerge()


    override fun updateStateWithResult(
        currentState: UserInfoState,
        lce: Lce<out UserInfoResult>
    ): UserInfoState = when (lce) {
        is Lce.Loading -> currentState.copy(loading = true)
        is Lce.Error -> currentState.copy(loading = false) //TODO: error
        is Lce.Content -> when (val result = lce.content) {
                is UserInfoResult.ScreenLoad -> currentState.copy(loading = false, items = result.items)
            is UserInfoResult.TextChange -> when (result) {
                is UserInfoResult.TextChange -> currentState.copy(
                    items = currentState.items.updated(result))
                else -> currentState.copy(
                    items = currentState.items.updated(result))
            }
            is UserInfoResult.Geocoded -> currentState.copy(
                items = currentState.items.updated(result))
            else -> currentState //TODO
        }
    }

    override fun Flow<Lce<out UserInfoResult>>.resultToEffects(): Flow<UserInfoEffect> = flatMapConcat {
        when (it) {
            is Lce.Error -> flowOf(
                if (it.throwable.isConflictApiError())
                    UserInfoEffect.ConflictError
                else
                    UserInfoEffect.Error(it.throwable)
            )
            is Lce.Content -> when (it.content) {
                is UserInfoResult.StartLogin -> flowOf(UserInfoEffect.StartLogin)
                is UserInfoResult.PrimaryButtonClick -> flowOf(UserInfoEffect.ShowHome)
                else -> emptyFlow()
            }
            else -> emptyFlow()
        }
    }

    private fun Flow<UserInfoEvent.ScreenLoad>.onScreenLoad() = flatMapLatest {
        viewState
            .take(1)
            .map {
                UserInfoResult.ScreenLoad(
                    if (it.items.isEmpty())
                        when (mode) {
                            UserInfoMode.LOGIN -> listOf(
                                UserInfoAdapterItem.UserEmail(),
                                UserInfoAdapterItem.UserPassword()
                            )
                            else -> listOf(
                                UserInfoAdapterItem.UserName(),
                                UserInfoAdapterItem.UserEmail(),
                                UserInfoAdapterItem.UserPassword(),
                                UserInfoAdapterItem.UserAddress()
                            )
                        }
                    else
                        it.items
                )
            }.wrapWithLce()
    }

    private fun Flow<UserInfoEvent.TextChange>.onTextChange() = flatMapLatest { event ->
        val textChanges = flowOf(
            UserInfoResult.TextChange(
                event.text, event.item
            )
        )
        val addressLookup = flow<UserInfoResult> {
            when (event.item) {
                is UserInfoAdapterItem.UserAddress -> {
                    if (event.text.isValidAddress()) {
                        val response = geocoder.geocode(event.text)
                        Logger.DEFAULT.log("UserInfoVM geocoded $response from ${event.text}")
                        emit(UserInfoResult.Geocoded(response))
                    }
                }
            }
        }
            .flowOn(Dispatchers.Default)

        flowOf(
            textChanges
                .map { Lce.Content(it) },
            addressLookup
                .wrapWithLce(false)
        ).flattenMerge(concurrency = 1)

    }

    private fun Flow<UserInfoEvent.FocusLost>.onFocusLost() = map { event ->
        UserInfoResult.FocusLost(event.text, event.item)
    }
        .map { Lce.Content(it) as Lce<UserInfoResult> }
        .catch { emit(Lce.Error(it)) }

    private fun Flow<UserInfoEvent.PrimaryButtonClick>.onPrimaryButtonClick() = flatMapLatest {
        viewState
            .take(1)
            .flatMapMerge {
                when (mode) {
                    UserInfoMode.REGISTER -> it.userRegistration(it.address)?.let {
                        userRepo.registerUser(it)
                    } ?: emptyFlow()
                    UserInfoMode.LOGIN -> userRepo.login(it.email!!, it.password!!)
                    else -> emptyFlow()
                }
            }
            .map {
                UserInfoResult.PrimaryButtonClick
            }
            .wrapWithLce()
    }

    private fun Flow<UserInfoEvent.SecondaryButtonClick>.onSecondaryButtonClick() =
        flatMapLatest {
            when (mode) {
                UserInfoMode.REGISTER -> flowOf(UserInfoResult.StartLogin)
                else -> emptyFlow()
            }
        }.map { Lce.Content(it) }


    private fun List<UserInfoAdapterItem>.updated(result: UserInfoResult): List<UserInfoAdapterItem> = map {
        when {
            result is UserInfoResult.Geocoded && it is UserInfoAdapterItem.UserAddress -> it.copy(
                address = result.addressSuggestions.firstOrNull(),
                addressSuggestions = result.addressSuggestions.map { it.displayValue }
            )
            result is UserInfoResult.TextChange && result.item::class == it::class -> when (it) {
                is UserInfoAdapterItem.UserName -> it.copy(result.text)
                is UserInfoAdapterItem.UserEmail -> it.copy(result.text)
                is UserInfoAdapterItem.UserPassword -> it.copy(result.text)
                is UserInfoAdapterItem.UserAddress -> it.copy(result.text)
            }
            else -> it
        }
    }
}

const val ADDRESS_REGEX = "^[A-Z0-9._%+-]+\\s.+\\s[0-9]{5,}$"
private fun String.isValidAddress() = Regex(ADDRESS_REGEX, RegexOption.IGNORE_CASE).matches(this)

val Address.displayValue get() = listOf(street, city, state, postalCode).filterNotNull().joinToString(", ")
