package com.polytron.auctionapp.di

import android.content.Context
import com.polytron.auctionapp.core.logging.AndroidLogger
import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.data.local.AndroidUserPreferencesRepository
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.SSE
import org.koin.dsl.module

val androidPlatformModule = module {
    single<AppLogger> { AndroidLogger() }
    single<UserPreferencesRepository> { AndroidUserPreferencesRepository(context = get<Context>()) }
    single {
        HttpClient(CIO) {
            install(SSE)
        }
    }

    // Android logger, preferences, and exporters are registered in later phases.
}
