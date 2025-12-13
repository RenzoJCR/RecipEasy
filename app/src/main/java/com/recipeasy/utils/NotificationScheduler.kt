package com.recipeasy.utils

import android.content.Context
import android.os.Handler
import android.os.Looper

class NotificationScheduler(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private val notificationHelper = NotificationHelper(context)

    companion object {
        private const val NOTIFICATION_INTERVAL = 60000L // 60 segundos
    }

    /**
     * Inicia las notificaciones cada 10 segundos
     */
    fun startNotifications() {
        if (isRunning) return

        isRunning = true
        handler.postDelayed(notificationRunnable, NOTIFICATION_INTERVAL)
    }

    /**
     * Detiene las notificaciones
     */
    fun stopNotifications() {
        isRunning = false
        handler.removeCallbacks(notificationRunnable)
    }

    /**
     * Comprueba si las notificaciones están activas
     */
    fun isNotificationsRunning(): Boolean = isRunning

    private val notificationRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                // Mostrar notificación aleatoria
                notificationHelper.showRandomNotification()

                // Programar la siguiente notificación
                handler.postDelayed(this, NOTIFICATION_INTERVAL)
            }
        }
    }

    /**
     * Cambia el intervalo de notificaciones
     */
    fun setNotificationInterval(intervalMillis: Long) {
        stopNotifications()
        // Aquí podrías cambiar el intervalo si quisieras
        startNotifications()
    }
}