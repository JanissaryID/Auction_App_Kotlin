package com.polytron.auctionapp.di

import com.polytron.auctionapp.data.api.ItemApiService
import com.polytron.auctionapp.data.api.KtorClient
import com.polytron.auctionapp.data.datastore.UserPreferences
import com.polytron.auctionapp.repositories.ItemsRepository
import com.polytron.auctionapp.repositories.ItemsRepositoryImpl
import com.polytron.auctionapp.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { KtorClient.httpClient }
    single { UserPreferences(androidContext()) }
    single { ItemApiService(client = get(), baseUrl = "https://gzip-hanging-immigration-prospect.trycloudflare.com/api/collections") }
    single<ItemsRepository> { ItemsRepositoryImpl(service = get()) }
    viewModel { MainViewModel(userPreferences = get(), repository = get()) }
}