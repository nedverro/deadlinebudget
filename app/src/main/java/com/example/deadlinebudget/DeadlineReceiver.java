package com.example.deadlinebudget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeadlineReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("task_id", -1);
        int devoirId = intent.getIntExtra("devoir_id", -1);
        String titre = intent.getStringExtra("titre");
        String message = intent.getStringExtra("message");

        int id = (taskId != -1) ? taskId : devoirId;

        if (id != -1) {
            NotificationHelper.showDeadlineNotification(context, titre, message, id);
        }
    }
}
