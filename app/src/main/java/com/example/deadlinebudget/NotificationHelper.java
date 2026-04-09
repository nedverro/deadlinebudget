package com.example.deadlinebudget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.deadlinebudget.activities.DetailActivity;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    public static final String CHANNEL_ID = "deadline_notifications_v5";
    private static final String CHANNEL_NAME = "Rappels de Deadlines";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications pour les échéances de tâches");
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notification créé: " + CHANNEL_ID);
            }
        }
    }

    public static void showDeadlineNotification(Context context, String titre, String message, int id) {
        sendDeadlineNotification(context, titre, message, id, null);
    }

    public static void cancelAllNotificationsForTask(Context context, int id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            String[] types = {"J7", "J3", "J1", "RETARD"};
            for (String type : types) {
                manager.cancel(id + type.hashCode());
            }
            manager.cancel(id);
            Log.d(TAG, "Notifications annulées pour la tâche ID: " + id);
        }
    }

    public static void sendDeadlineNotification(Context context, String titre, String message, int id, String type) {
        int notificationId = id + (type != null ? type.hashCode() : 0);
        Log.d(TAG, "Tentative d'envoi de notification: " + titre + " (ID=" + notificationId + ")");

        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("task_id", id);
        intent.putExtra("from_notification", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int icon = android.R.drawable.ic_dialog_alert;
        int color = 0xFFF44336;

        if (type != null) {
            switch(type) {
                case "J7": icon = android.R.drawable.ic_dialog_info; color = 0xFFFF9800; break;
                case "J3": icon = android.R.drawable.ic_dialog_alert; color = 0xFFFF6600; break;
                case "J1": icon = android.R.drawable.ic_dialog_alert; color = 0xFFF44336; break;
                case "RETARD": icon = android.R.drawable.stat_notify_error; color = 0xFF9C27B0; break;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(titre)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(color)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification affichée avec succès (ID=" + notificationId + ")");
        }
    }
}