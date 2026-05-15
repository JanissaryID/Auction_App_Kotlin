package com.polytron.auctionapp.di

import com.polytron.auctionapp.presentation.auction.AuctionViewModel
import com.polytron.auctionapp.presentation.auth.AuthViewModel
import com.polytron.auctionapp.presentation.items.ItemsViewModel
import com.polytron.auctionapp.presentation.payment.PaymentViewModel
import com.polytron.auctionapp.presentation.pickup.PickupViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

val presentationModule = module {
    factory { (scope: CoroutineScope) ->
        AuthViewModel(
            scope = scope,
            loginUseCase = get(),
            restoreSessionUseCase = get(),
            logoutUseCase = get(),
            userPreferencesRepository = get(),
            sessionManager = get(),
            logger = get()
        )
    }
    factory { (scope: CoroutineScope) ->
        ItemsViewModel(
            scope = scope,
            fetchItemsUseCase = get(),
            createItemUseCase = get(),
            updateItemUseCase = get(),
            deleteItemUseCase = get(),
            observeItemsRealtimeUseCase = get(),
            subscribeItemsRealtimeUseCase = get(),
            sessionManager = get(),
            logger = get()
        )
    }
    factory { (scope: CoroutineScope) -> AuctionViewModel(get(), scope) }
    factory { PaymentViewModel() }
    factory { PickupViewModel() }
}
