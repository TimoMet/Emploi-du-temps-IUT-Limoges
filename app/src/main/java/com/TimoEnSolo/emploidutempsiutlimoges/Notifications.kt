package com.TimoEnSolo.emploidutempsiutlimoges

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object Notifications {
    private const val NEW_EDT = "NewEDT"
    private const val EDT_CHANGED = "EDTChanged"
    private const val GROUP_EDT_CHANGED = "com.unimolix.emploidutempsiutlimoges.EDTChanged"
    private fun createNewEdtNotificationChannel(context: Context) {
        val name: CharSequence = "nouveaux emploi du temps"
        val description = "notification envoyé quand un nouvel emploi du temps est disponible"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NEW_EDT, name, importance)
        channel.description = description
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createEdtChangedNotificationChannel(context: Context) {
        val name: CharSequence = "changement emploi du temps"
        val description = "notification envoyé quand un emploi du temps est changé"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(EDT_CHANGED, name, importance)
        channel.description = description
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotificationChannels(context: Context) {
        createNewEdtNotificationChannel(context)
        createEdtChangedNotificationChannel(context)
    }

    fun createNewEdtNotification(context: Context, newEdt: Int) {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("notifNewEdt", true)) {
            return
        }
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, NEW_EDT)
            .setContentTitle("Nouvel emploi du temps")
            .setContentText("L'emploi du temps de la semaine $newEdt est disponible")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.logo_edt)
        NotificationManagerCompat.from(context).notify(0, builder.build())
    }

    fun createEdtChangedNotification(context: Context, edtChanged: Int) {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("notifEdtChanged", true)) {
            return
        }
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, EDT_CHANGED)
            .setContentTitle("Changement emploi du temps")
            .setContentText("L'emploi du temps de la semaine $edtChanged a été changé")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_EDT_CHANGED)
            .setSmallIcon(R.drawable.logo_edt)
        NotificationManagerCompat.from(context).notify(edtChanged, builder.build())
    }

    fun createEdtChangedSummaryNotification(context: Context) {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("notifEdtChanged", true)) {
            return
        }
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, EDT_CHANGED)
            .setContentTitle("Changement emploi du temps")
            .setContentText("Plusieurs emplois du temps ont été changés")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_EDT_CHANGED)
            .setGroupSummary(true)
            .setSmallIcon(R.drawable.logo_edt)
        NotificationManagerCompat.from(context).notify(1000, builder.build())
    }
}