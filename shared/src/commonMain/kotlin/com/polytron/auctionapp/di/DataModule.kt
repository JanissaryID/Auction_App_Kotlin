package com.polytron.auctionapp.di

import com.polytron.auctionapp.data.repository.ItemsRepositoryImpl
import com.polytron.auctionapp.domain.repository.ItemsRepository
import com.polytron.auctionapp.domain.session.SessionExpiryHandler
import com.polytron.auctionapp.domain.session.SessionManager
import org.koin.dsl.module

val dataModule = module {
    single { SessionManager(userPreferencesRepository = get(), logger = get()) }
    single<SessionExpiryHandler> { get<SessionManager>() }
    single<ItemsRepository> {
        ItemsRepositoryImpl(
            httpClient = get(),
            sessionExpiryHandler = get()
        )
    }
}
