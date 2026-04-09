package com.example.deadlinebudget;

import android.app.Application;
import com.example.deadlinebudget.data.DataManager;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DeadlineBudgetApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("deadlinebudget.realm")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .compactOnLaunch()
                .schemaVersion(1) // Set schema version to 1 to match the existing database
                .deleteRealmIfMigrationNeeded() // Handle schema changes during development
                .build();
        Realm.setDefaultConfiguration(config);
        
        // Initialize DataManager
        DataManager.init(this);

        // Initialize Notifications
        NotificationHelper.createNotificationChannel(this);
        TaskReminderManager.rescheduleAllReminders(this);
    }
}