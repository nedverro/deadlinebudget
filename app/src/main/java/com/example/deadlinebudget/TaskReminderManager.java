package com.example.deadlinebudget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.Tache;
import com.example.deadlinebudget.models.TacheEmploi;

public class TaskReminderManager {
    private static final String TAG = "TaskReminderManager";

    public static void scheduleRemindersForTask(Context context, Tache tache) {
        if (tache.isEstTerminee()) {
            cancelRemindersForTask(context, tache.getId(), true);
            return;
        }
        scheduleAllReminders(context, tache.getId(), tache.getTitre(), tache.getDateLimite(), true);
    }

    public static void scheduleRemindersForDevoir(Context context, TacheEmploi devoir) {
        if (devoir.getStatut() == 2) {
            cancelRemindersForTask(context, devoir.getId(), false);
            return;
        }
        scheduleAllReminders(context, devoir.getId(), devoir.getNom(), devoir.getDeadlineTimestamp(), false);
    }

    private static void scheduleAllReminders(Context context, int id, String titre, long deadline, boolean isTask) {
        // 7 jours, 3 jours, 1 jour avant
        scheduleIfValid(context, id, titre, deadline - (7L * 24 * 60 * 60 * 1000), "J7", isTask);
        scheduleIfValid(context, id, titre, deadline - (3L * 24 * 60 * 60 * 1000), "J3", isTask);
        scheduleIfValid(context, id, titre, deadline - (1L * 24 * 60 * 60 * 1000), "J1", isTask);
        // Au moment exact de la deadline
        scheduleIfValid(context, id, titre, deadline, "RETARD", isTask);
    }

    private static void scheduleIfValid(Context context, int id, String titre, long triggerTime, String type, boolean isTask) {
        if (triggerTime > System.currentTimeMillis()) {
            scheduleSingleAlarm(context, id, titre, triggerTime, type, isTask);
        }
    }

    private static void scheduleSingleAlarm(Context context, int id, String titre, long triggerTime, String type, boolean isTask) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_id", id);
        intent.putExtra("titre", titre);
        intent.putExtra("type", type);
        intent.putExtra("is_task", isTask);

        int requestCode = getUniqueRequestCode(id, type, isTask);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if ("RETARD".equals(type)) {
                // Utilisation de AlarmClock pour une précision absolue à la seconde près
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(triggerTime, pendingIntent);
                alarmManager.setAlarmClock(info, pendingIntent);
                Log.d(TAG, "Alarme CRITIQUE (Clock) planifiée pour " + titre + " à " + triggerTime);
            } else {
                // Pour les rappels normaux (J-7, J-3, etc.)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
                Log.d(TAG, "Rappel planifié pour " + titre + " (" + type + ") à " + triggerTime);
            }
        } catch (SecurityException e) {
            // Repli si la permission d'alarme exacte est manquante
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.e(TAG, "Permission d'alarme exacte manquante, utilisation d'une alarme inexacte");
        }
    }

    public static void cancelRemindersForTask(Context context, int id, boolean isTask) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        String[] types = {"J7", "J3", "J1", "RETARD"};
        for (String type : types) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            int requestCode = getUniqueRequestCode(id, type, isTask);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    private static int getUniqueRequestCode(int id, String type, boolean isTask) {
        int typeCode = 0;
        switch (type) {
            case "J7": typeCode = 1; break;
            case "J3": typeCode = 2; break;
            case "J1": typeCode = 3; break;
            case "RETARD": typeCode = 4; break;
        }
        return (isTask ? 1000000 : 2000000) + (id * 10) + typeCode;
    }

    public static void rescheduleAllReminders(Context context) {
        try {
            DataManager dm = DataManager.getInstance();
            for (Tache t : dm.getAllTaches()) scheduleRemindersForTask(context, t);
            for (TacheEmploi d : dm.getAllTacheEmplois()) scheduleRemindersForDevoir(context, d);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du recalcul des rappels", e);
        }
    }
}