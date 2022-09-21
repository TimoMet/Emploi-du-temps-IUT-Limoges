package com.unimolix.emploidutempsiutlimoges;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifications {
    private static final String NEW_EDT = "NewEDT";
    private static final String EDT_CHANGED = "EDTChanged";
    private static final String GROUP_EDT_CHANGED = "com.unimolix.emploidutempsiutlimoges.EDTChanged";

    private static void createNewEdtNotificationChannel(Context context) {

        CharSequence name = "nouveaux emploi du temps";
        String description = "notification envoyé quand un nouvel emploi du temps est disponible";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(NEW_EDT, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private static void createEdtChangedNotificationChannel(Context context) {

        CharSequence name = "changement emploi du temps";
        String description = "notification envoyé quand un emploi du temps est changé";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(EDT_CHANGED, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    public static void createNotificationChannels(Context context) {
        createNewEdtNotificationChannel(context);
        createEdtChangedNotificationChannel(context);
    }


    public static void createNewEdtNotification(Context context, int newEdt) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("notifNewEdt", true)) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NEW_EDT)
                .setContentTitle("Nouvel emploi du temps")
                .setContentText("L'emploi du temps de la semaine " + newEdt + " est disponible")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.logo_edt);
        NotificationManagerCompat.from(context).notify(0, builder.build());
    }

    public static void createEdtChangedNotification(Context context, int edtChanged) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("notifEdtChanged", true)) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EDT_CHANGED)
                .setContentTitle("Changement emploi du temps")
                .setContentText("L'emploi du temps de la semaine " + edtChanged + " a été changé")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setGroup(GROUP_EDT_CHANGED)
                .setSmallIcon(R.drawable.logo_edt);

        NotificationManagerCompat.from(context).notify(edtChanged, builder.build());

    }

    public static void createEdtChangedSummaryNotification(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("notifEdtChanged", true)) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EDT_CHANGED)
                .setContentTitle("Changement emploi du temps")
                .setContentText("Plusieurs emplois du temps ont été changés")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setGroup(GROUP_EDT_CHANGED)
                .setGroupSummary(true)
                .setSmallIcon(R.drawable.logo_edt);

        NotificationManagerCompat.from(context).notify(1000, builder.build());
    }
}
