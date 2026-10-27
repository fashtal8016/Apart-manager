package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    fun shareFile(context: Context, file: File, mimeType: String, title: String = "اشتراک‌گذاری گزارش") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            // Fallback: share file via simple stream or text
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "گزارش ساختمان: ${file.name}")
            }
            context.startActivity(Intent.createChooser(intent, title))
        }
    }

    fun shareSms(context: Context, phoneNumber: String, messageText: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", messageText)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
            }
            context.startActivity(Intent.createChooser(shareIntent, "ارسال پیام به ساکن"))
        }
    }
}
