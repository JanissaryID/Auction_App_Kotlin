package com.polytron.auctionapp.di

import com.polytron.auctionapp.data.datastore.UserPreferences
import com.polytron.auctionapp.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { UserPreferences(androidContext()) }
    viewModel { MainViewModel(userPreferences = get()) }
}