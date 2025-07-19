package com.polytron.auctionapp.di

import androidx.activity.ComponentActivity
import com.polytron.auctionapp.data.datastore.UserPreferences
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { UserPreferences(androidContext()) }
    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }
    single { MainViewModel(userPreferences = get()) }
}