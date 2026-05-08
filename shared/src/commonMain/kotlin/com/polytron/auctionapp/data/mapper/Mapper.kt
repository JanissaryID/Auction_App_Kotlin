package com.polytron.auctionapp.data.mapper

fun interface Mapper<in Input, out Output> {
    fun map(input: Input): Output
}
