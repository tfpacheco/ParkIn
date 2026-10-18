package com.parkin.app.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.parkin.app.R
import kotlinx.coroutines.*
import java.util.Locale

class ParkingNotificationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isParkingActive = false
    private var startTimeMillis: Long = 0L
    private var pricePerHour: Double = 1.20 // Exemplo: 1.20€ por hora

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_PARKING -> {
                startTimeMillis = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                pricePerHour = intent.getDoubleExtra(EXTRA_PRICE_PER_HOUR, 1.20)
                startParkingTracking()
            }
            ACTION_STOP_PARKING -> {
                stopParkingTracking()
            }
        }
        return START_STICKY
    }

    private fun startParkingTracking() {
        createNotificationChannel()
        isParkingActive = true

        startForeground(NOTIFICATION_ID, buildNotification("00:00:00", "0.00€"))

        serviceScope.launch {
            while (isParkingActive) {
                val elapsedMillis = System.currentTimeMillis() - startTimeMillis
                val seconds = (elapsedMillis / 1000) % 60
                val minutes = (elapsedMillis / (1000 * 60)) % 60
                val hours = elapsedMillis / (1000 * 60 * 60)
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                val hoursElapsed = elapsedMillis.toDouble() / (1000 * 60 * 60)
                val currentCost = hoursElapsed * pricePerHour
                val formattedCost = String.format(Locale.getDefault(), "%.2f€", currentCost)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, buildNotification(formattedTime, formattedCost))

                delay(1000) // Atualiza a cada 1 segundo
            }
        }
    }

    private fun stopParkingTracking() {
        isParkingActive = false
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(time: String, cost: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Estacionamento Ativo - ParkIn")
            .setContentText("Tempo: $time | Total: $cost")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Estacionamento em Tempo Real",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mostra o tempo decorrido e custo do parqueamento na lock screen"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "parkin_realtime_parking"
        const val ACTION_START_PARKING = "ACTION_START_PARKING"
        const val ACTION_STOP_PARKING = "ACTION_STOP_PARKING"
        const val EXTRA_START_TIME = "EXTRA_START_TIME"
        const val EXTRA_PRICE_PER_HOUR = "EXTRA_PRICE_PER_HOUR"
    }
}