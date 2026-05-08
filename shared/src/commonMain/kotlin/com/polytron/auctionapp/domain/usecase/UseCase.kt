package com.polytron.auctionapp.domain.usecase

fun interface UseCase<in Params, out Result> {
    suspend operator fun invoke(params: Params): Result
}

fun interface NoParamsUseCase<out Result> {
    suspend operator fun invoke(): Result
}
