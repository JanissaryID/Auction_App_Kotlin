package com.polytron.auctionapp.core.logging

class DesktopLogger : AppLogger {
    override fun debug(tag: String, message: String) {
        println("D/$tag: $message")
    }

    override fun info(tag: String, message: String) {
        println("I/$tag: $message")
    }

    override fun warn(tag: String, message: String) {
        println("W/$tag: $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        println("E/$tag: $message")
        throwable?.printStackTrace()
    }
}
