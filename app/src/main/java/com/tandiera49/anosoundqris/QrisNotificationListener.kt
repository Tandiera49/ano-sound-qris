package com.tandiera49.anosoundqris

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class QrisNotificationListener : NotificationListenerService() {
    private var tts: TextToSpeech? = null
    private var ready = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("id", "ID"))
                ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setSpeechRate(0.92f)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val n = sbn.notification ?: return
        if ((n.flags and Notification.FLAG_ONGOING_EVENT) != 0) return
        val extras = n.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val body = listOf(
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty(),
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
            extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()
        ).filter { it.isNotBlank() }.distinct().joinToString(" ")

        // Strictly accept GoPay Merchant's QRIS-success notification, not arbitrary messages.
        val appLabel = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        } catch (_: Exception) { "" }
        val trustedSender = appLabel.contains("GoPay", true) || sbn.packageName.contains("gopay", true) ||
            (sbn.packageName.contains("gojek", true) && appLabel.contains("Merchant", true))
        val qrisSuccess = title.contains("Pembayaran QRIS", true) &&
            (title.contains("diterima", true) || body.contains("QRIS", true) && body.contains("diterima", true))
        if (!trustedSender || !qrisSuccess) return

        val amount = Regex("(?i)Rp\\s*([0-9][0-9.,]*)").find(body)?.groupValues?.getOrNull(1)
            ?.filter { it.isDigit() }?.toLongOrNull() ?: return
        if (amount <= 0) return

        val eventTime = if (n.`when` > 0) n.`when` else sbn.postTime
        val eventKey = "${sbn.packageName}|${sbn.id}|${sbn.tag}|$title|$body|$eventTime"
        if (alreadyProcessed(eventKey)) return

        val phrase = "Pembayaran diterima, ${toIndonesianWords(amount)} rupiah"
        if (ready) tts?.speak(phrase, TextToSpeech.QUEUE_ADD, null, eventKey)
    }

    private fun alreadyProcessed(key: String): Boolean {
        val prefs = getSharedPreferences("qris_seen", Context.MODE_PRIVATE)
        if (prefs.getBoolean(key, false)) return true
        val all = prefs.all.keys.toMutableList()
        val editor = prefs.edit().putBoolean(key, true)
        if (all.size > 250) {
            all.take(all.size - 200).forEach { editor.remove(it) }
        }
        editor.apply()
        return false
    }

    private fun toIndonesianWords(value: Long): String {
        if (value == 0L) return "nol"
        val units = arrayOf("", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan", "sepuluh", "sebelas")
        fun words(n: Long): String = when {
            n < 12 -> units[n.toInt()]
            n < 20 -> words(n - 10) + " belas"
            n < 100 -> words(n / 10) + " puluh" + if (n % 10 != 0L) " " + words(n % 10) else ""
            n < 200 -> "seratus" + if (n > 100) " " + words(n - 100) else ""
            n < 1000 -> words(n / 100) + " ratus" + if (n % 100 != 0L) " " + words(n % 100) else ""
            n < 2000 -> "seribu" + if (n > 1000) " " + words(n - 1000) else ""
            n < 1_000_000 -> words(n / 1000) + " ribu" + if (n % 1000 != 0L) " " + words(n % 1000) else ""
            n < 1_000_000_000 -> words(n / 1_000_000) + " juta" + if (n % 1_000_000 != 0L) " " + words(n % 1_000_000) else ""
            else -> words(n / 1_000_000_000) + " miliar" + if (n % 1_000_000_000 != 0L) " " + words(n % 1_000_000_000) else ""
        }
        return words(value)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
