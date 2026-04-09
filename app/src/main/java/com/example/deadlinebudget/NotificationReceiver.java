package com.example.deadlinebudget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("task_id", -1);
        String titre = intent.getStringExtra("titre");
        String type = intent.getStringExtra("type");
        Log.d(TAG, "Alarme reçue pour " + titre + " (Type: " + type + ", ID: " + id + ")");

        if (id == -1) return;

        String message = "";
        String prefix = "";
        
        if (type == null) type = "RETARD";

        switch(type) {
            case "J7":
                prefix = "⚠️ ";
                message = prefix + titre + " - à rendre dans 7 jours !";
                break;
            case "J3":
                prefix = "⚠️ ";
                message = prefix + titre + " - à rendre dans 3 jours !";
                break;
            case "J1":
                prefix = "🔴 ";
                message = prefix + titre + " - À RENDRE DEMAIN !";
                break;
            case "RETARD":
                prefix = "❌ ";
                message = prefix + titre + " - La deadline est dépassée !";
                break;
            default:
                message = titre;
        }
        
        NotificationHelper.sendDeadlineNotification(context, "Échéance proche !", message, id, type);
    }
}