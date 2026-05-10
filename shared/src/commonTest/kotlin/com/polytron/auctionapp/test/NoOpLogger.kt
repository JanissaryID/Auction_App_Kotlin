package com.polytron.auctionapp.test

import com.polytron.auctionapp.core.logging.AppLogger

/**
 * No-op logger for tests. Does not print anything.
 * Use this as default logger in all test classes.
 */
object NoOpLogger : AppLogger {
    override fun debug(tag: String, message: String) = Unit
    override fun info(tag: String, message: String) = Unit
    override fun warn(tag: String, message: String) = Unit
    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
}
