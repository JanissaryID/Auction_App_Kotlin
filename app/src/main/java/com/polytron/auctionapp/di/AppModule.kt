package com.polytron.auctionapp.di

import androidx.activity.ComponentActivity
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.ui.viewmodel.AuctionViewModel
import com.polytron.auctionapp.ui.viewmodel.AuthViewModel
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.ui.viewmodel.PrinterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // --- Bluetooth ---
    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }

    // --- ViewModels ---
    viewModel {
        AuthViewModel(
            loginUseCase = get(),
            restoreSessionUseCase = get(),
            logoutUseCase = get(),
            userPreferencesRepository = get(),
            sessionManager = get(),
            logger = get()
        )
    }
    viewModel {
        ItemsViewModel(
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
    viewModel { AuctionViewModel(delegate = get()) }
    viewModel { PrinterViewModel() }
}
