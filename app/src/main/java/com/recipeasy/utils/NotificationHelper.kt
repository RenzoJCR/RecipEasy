package com.recipeasy.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.recipeasy.R
import com.recipeasy.activities.MainActivity
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.models.Receta

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "recipeasy_notifications"
        const val CHANNEL_NAME = "Recetas y Recordatorios"
        const val NOTIFICATION_ID = 100

        // Tipos de mensajes
        private val recipeMessages = listOf(
            "🍽️ ¿Listo para cocinar? Prueba: ",
            "👨‍🍳 Te recomendamos: ",
            "🥘 ¡Nueva sugerencia! ",
            "🍳 Perfecta para hoy: ",
            "👩‍🍳 ¿Has probado? "
        )

        private val reminderMessages = listOf(
            "⏰ ¡Hora de cocinar algo delicioso!",
            "🍛 Tu próxima comida favorita te espera",
            "🥗 ¿Ya planificaste qué preparar hoy?",
            "👶 ¡Receta fácil para principiantes!",
            "🔥 ¡Calienta esos fuegos! Es hora de cocinar"
        )
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH  // Cambiado a HIGH para que sea más visible
            ).apply {
                description = "Recetas sugeridas y recordatorios de cocina"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Muestra una notificación con una receta sugerida
     */
    fun showRecipeNotification() {
        // Verificar si tenemos permiso (solo Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                return  // No mostrar si no hay permiso
            }
        }

        val dbHelper = DatabaseHelper(context)
        val allRecipes = dbHelper.getAllRecipes()

        if (allRecipes.isEmpty()) return

        val randomRecipe = allRecipes.random()
        val randomMessage = recipeMessages.random()

        // Intent para abrir MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("FROM_NOTIFICATION", true)
            putExtra("RECIPE_NAME", randomRecipe.nombre)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(), // ID único basado en tiempo
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("RecipEasy")
            .setContentText("$randomMessage${randomRecipe.nombre}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$randomMessage${randomRecipe.nombre}\n\n" +
                            "${randomRecipe.descripcion}\n\n" +
                            "⏱️ ${randomRecipe.tiempoPreparacion} min | " +
                            "⚡ ${randomRecipe.dificultad}")
            )
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Alta prioridad
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)  // Alertar cada vez
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)

        // Mostrar notificación con ID único
        val notificationId = NOTIFICATION_ID + (System.currentTimeMillis() % 1000).toInt()
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    /**
     * Muestra una notificación de recordatorio
     */
    fun showReminderNotification() {
        // Verificar permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                return
            }
        }

        val randomMessage = reminderMessages.random()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("FROM_NOTIFICATION", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("RecipEasy")
            .setContentText(randomMessage)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)

        val notificationId = NOTIFICATION_ID + 1 + (System.currentTimeMillis() % 1000).toInt()
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    /**
     * Alterna entre tipos de notificaciones
     */
    fun showRandomNotification() {
        when ((0..1).random()) {
            0 -> showRecipeNotification()
            1 -> showReminderNotification()
        }
    }
}