package com.polytron.auctionapp.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardManagerCompat {
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Scanned Result", text)
        clipboard.setPrimaryClip(clip)
    }
}


