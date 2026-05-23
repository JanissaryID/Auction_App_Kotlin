package com.polytron.auctionapp.core.logging

import java.io.EOFException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.ClosedChannelException

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
        if (throwable == null) return

        if (throwable.isRecoverableNetworkFailure()) {
            println("  ${throwable.javaClass.simpleName}: ${throwable.message.orEmpty()}")
        } else {
            throwable.printStackTrace()
        }
    }

    private fun Throwable.isRecoverableNetworkFailure(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (
                current is SocketException ||
                current is SocketTimeoutException ||
                current is UnknownHostException ||
                current is EOFException ||
                current is ClosedChannelException
            ) {
                return true
            }

            val message = current.message?.lowercase().orEmpty()
            if (
                "connection reset" in message ||
                "connection refused" in message ||
                "connection closed" in message ||
                "connection abort" in message ||
                "network is unreachable" in message ||
                "no such host" in message ||
                "failed to connect" in message ||
                "timed out" in message ||
                "timeout" in message
            ) {
                return true
            }

            current = current.cause
        }
        return false
    }
}
