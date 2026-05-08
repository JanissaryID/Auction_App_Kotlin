package com.polytron.auctionapp.presentation

interface UiState

interface UiAction

interface UiEffect

/*
 * Phase 1A decision:
 * Prefer KMP-compatible ViewModels registered through Koin. If a later platform
 * constraint blocks that, use plain state holders with explicit CoroutineScope
 * while keeping the same UiState/UiAction/UiEffect contract.
 */
