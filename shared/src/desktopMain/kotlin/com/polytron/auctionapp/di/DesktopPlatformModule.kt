package com.polytron.auctionapp.di

import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.core.logging.DesktopLogger
import com.polytron.auctionapp.data.local.DesktopUserPreferencesRepository
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.SSE
import org.koin.dsl.module

val desktopPlatformModule = module {
    single<AppLogger> { DesktopLogger() }
    single<UserPreferencesRepository> { DesktopUserPreferencesRepository() }
    single {
        HttpClient(CIO) {
            install(SSE)
        }
    }

    // Desktop logger, preferences, and exporters are registered in later phases.
}
