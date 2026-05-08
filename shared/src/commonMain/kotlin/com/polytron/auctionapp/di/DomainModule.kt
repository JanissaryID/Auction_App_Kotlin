package com.polytron.auctionapp.di

import com.polytron.auctionapp.domain.usecase.auth.LoginUseCase
import com.polytron.auctionapp.domain.usecase.auth.LogoutUseCase
import com.polytron.auctionapp.domain.usecase.auth.RestoreSessionUseCase
import com.polytron.auctionapp.domain.usecase.items.CreateItemUseCase
import com.polytron.auctionapp.domain.usecase.items.DeleteItemUseCase
import com.polytron.auctionapp.domain.usecase.items.FetchItemsUseCase
import com.polytron.auctionapp.domain.usecase.items.UpdateItemUseCase
import com.polytron.auctionapp.domain.usecase.realtime.ObserveItemsRealtimeUseCase
import com.polytron.auctionapp.domain.usecase.realtime.SubscribeItemsRealtimeUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { LoginUseCase(repository = get(), preferences = get(), sessionManager = get()) }
    factory { LogoutUseCase(sessionManager = get()) }
    factory { RestoreSessionUseCase(repository = get(), preferences = get(), sessionManager = get()) }

    factory { FetchItemsUseCase(repository = get()) }
    factory { CreateItemUseCase(repository = get()) }
    factory { UpdateItemUseCase(repository = get()) }
    factory { DeleteItemUseCase(repository = get()) }

    factory { ObserveItemsRealtimeUseCase(repository = get()) }
    factory { SubscribeItemsRealtimeUseCase(repository = get()) }
}
