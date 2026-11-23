package com.tandiera49.anosoundqris

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private lateinit var accessStatus: TextView
    private lateinit var voiceStatus: TextView
    private lateinit var testButton: Button
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(16, 26, 43)
        window.navigationBarColor = android.graphics.Color.rgb(16, 26, 43)
        tts = TextToSpeech(this, this)
        buildScreen()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            setBackgroundColor(android.graphics.Color.rgb(11, 18, 32))
        }
        scroll.addView(root)

        val brand = TextView(this).apply {
            text = "ANO SOUND QRIS"
            textSize = 25f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(brand)
        root.addView(label("SOUNDBOX PEMBAYARAN GOPAY MERCHANT", 12f, 0xFF24D18B.toInt(), 10))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            setBackgroundColor(0xFF17243A.toInt())
        }
        val state = TextView(this).apply {
            text = "SIAP MENERIMA PEMBAYARAN"
            textSize = 12f
            setTextColor(0xFF9EB1C9.toInt())
        }
        card.addView(state)
        card.addView(TextView(this).apply {
            text = "Menunggu transaksi QRIS"
            textSize = 21f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dp(8), 0, 0)
        })
        card.addView(TextView(this).apply {
            text = "Sumber: notifikasi asli GoPay Merchant"
            textSize = 13f
            setTextColor(0xFFB8C5D6.toInt())
            setPadding(0, dp(8), 0, 0)
        })
        val params = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24) }
        root.addView(card, params)

        root.addView(section("PENGATURAN", 28))
        accessStatus = label("Memeriksa izin notifikasi…", 14f, 0xFFFFC66D.toInt(), 8)
        root.addView(accessStatus)
        val accessButton = button("AKTIFKAN AKSES NOTIFIKASI") {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
        root.addView(accessButton)

        voiceStatus = label("Suara: Bahasa Indonesia • mengikuti suara perempuan yang tersedia di mesin TTS HP", 13f, 0xFFB8C5D6.toInt(), 18)
        root.addView(voiceStatus)
        testButton = button("TES SUARA") { speakTest() }
        root.addView(testButton)

        root.addView(section("CARA MENGGUNAKAN", 28))
        root.addView(label("1. Aktifkan akses notifikasi untuk ANO SOUND QRIS.\n2. Pastikan notifikasi GoPay Merchant diizinkan.\n3. Biarkan aplikasi tetap terpasang.\n4. Saat notifikasi 'Pembayaran QRIS statis diterima' masuk, nominal akan dibacakan.", 14f, 0xFFE1E8F0.toInt(), 10))
        root.addView(label("Catatan: suara perempuan bergantung pada pilihan voice Bahasa Indonesia yang tersedia di mesin Text-to-Speech Android. Jika belum ada, pasang/unduh voice Bahasa Indonesia di pengaturan Text-to-Speech HP.", 12f, 0xFF8FA2BA.toInt(), 18))
        setContentView(scroll)
    }

    private fun speakTest() {
        if (ttsReady) tts?.speak("Pembayaran diterima, dua ribu lima ratus rupiah", TextToSpeech.QUEUE_FLUSH, null, "ano-test")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("id", "ID"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            runOnUiThread {
                voiceStatus.text = if (ttsReady) "Suara: Bahasa Indonesia aktif • gunakan voice perempuan pada pengaturan TTS HP" else "Voice Bahasa Indonesia belum tersedia. Unduh voice Bahasa Indonesia di pengaturan Text-to-Speech HP."
                testButton.isEnabled = ttsReady
                testButton.alpha = if (ttsReady) 1f else 0.5f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::accessStatus.isInitialized) {
            val enabled = isListenerEnabled()
            accessStatus.text = if (enabled) "● Akses notifikasi aktif" else "● Akses notifikasi belum aktif"
            accessStatus.setTextColor(if (enabled) 0xFF24D18B.toInt() else 0xFFFFC66D.toInt())
        }
    }

    private fun isListenerEnabled(): Boolean {
        val expected = "$packageName/${QrisNotificationListener::class.java.name}"
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return enabled.split(":").any { it.equals(expected, true) }
    }

    private fun section(text: String, top: Int) = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTextColor(0xFF24D18B.toInt())
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setPadding(0, dp(top), 0, dp(10))
    }
    private fun label(text: String, size: Float, color: Int, bottom: Int) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        setPadding(0, 0, 0, dp(bottom))
    }
    private fun button(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setTextColor(0xFF07131B.toInt())
        setBackgroundColor(0xFF24D18B.toInt())
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(10) }
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
