package com.polytron.auctionapp.desktop.di

import com.polytron.auctionapp.desktop.auth.AuthManager
import com.polytron.auctionapp.desktop.data.DesktopRemoteItemsRepository
import com.polytron.auctionapp.desktop.data.remote.PocketBaseRepository
import com.polytron.auctionapp.shared.repository.ItemsRepository
import com.polytron.auctionapp.shared.viewmodel.ItemsSharedViewModel
import org.koin.dsl.module

val desktopModule = module {
    // PocketBase API client
    single { PocketBaseRepository() }

    // Authentication (with PocketBase)
    single { AuthManager(pocketBaseRepository = get()) }

    // Repository (backed by PocketBase API)
    single<ItemsRepository> { DesktopRemoteItemsRepository(pocketBaseRepository = get()) }

    // Shared ViewModel
    single { ItemsSharedViewModel(repository = get()) }
}
