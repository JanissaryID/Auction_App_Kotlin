package com.polytron.auctionapp.di

import com.polytron.auctionapp.data.api.KtorClient
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import org.koin.dsl.module

val appModule = module {
//    single { DataStore(androidContext()) }
    single { KtorClient.httpClient }
    single { ItemsViewModel() }
}