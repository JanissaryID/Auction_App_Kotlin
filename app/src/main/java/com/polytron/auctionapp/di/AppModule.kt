package com.polytron.auctionapp.di

import androidx.activity.ComponentActivity
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepository
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepositoryImpl
import com.polytron.auctionapp.data.remote.repository.ItemsRepository
import com.polytron.auctionapp.data.remote.repository.ItemsRepositoryImpl
import com.polytron.auctionapp.data.shared.AndroidSharedItemsRepository
import com.polytron.auctionapp.data.session.SessionManager
import com.polytron.auctionapp.ui.viewmodel.AuctionViewModel
import com.polytron.auctionapp.ui.viewmodel.AuthViewModel
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.ui.viewmodel.PrinterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // --- Bluetooth ---
    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }

    // --- Data Layer ---
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }
    single { SessionManager(userPreferencesRepository = get()) }
    single<ItemsRepository> { ItemsRepositoryImpl(sessionManager = get()) }
    single<com.polytron.auctionapp.shared.repository.ItemsRepository> {
        AndroidSharedItemsRepository(remoteRepository = get<ItemsRepository>())
    }

    // --- Shared ViewModels (Singleton) ---
    single { com.polytron.auctionapp.shared.viewmodel.ItemsSharedViewModel(repository = get()) }

    // --- ViewModels ---
    viewModel { AuthViewModel(userPreferencesRepository = get(), itemsRepository = get(), sessionManager = get()) }
    viewModel { ItemsViewModel(itemsRepository = get(), sessionManager = get()) }
    viewModel { AuctionViewModel() }
    viewModel { PrinterViewModel() }
}