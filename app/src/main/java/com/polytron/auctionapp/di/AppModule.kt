package com.polytron.auctionapp.di

import androidx.activity.ComponentActivity
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepository
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepositoryImpl
import com.polytron.auctionapp.data.local.viewmodel.UserPreferencesViewModel
import com.polytron.auctionapp.data.remote.repository.ItemsRepository
import com.polytron.auctionapp.data.remote.repository.ItemsRepositoryImpl
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }

    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }
    viewModel { UserPreferencesViewModel(get()) }

    single<ItemsRepository> { ItemsRepositoryImpl() }
    viewModel { InventoryViewModel(userPreferencesRepository = get(), itemsRepository = get()) }
}