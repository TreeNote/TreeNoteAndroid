package de.treenote.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import de.treenote.receiver.TreeNodeNotificationReceiver;

/**
 * Hier wird der Zeitpunkt für die Erstellung der Alarme festgelegt
 */
public class TreeNoteAlarmService extends IntentService {

    public static final String TAG = "TreeNoteAlarmService";
    public static final int INTERVAL_MILLIS = 1000 * 60 * 60 * 24; // 24 hours
    public static final int TRIGGER_HOUR_OF_DAY = 8;
    public static final int PENDING_INTENT_REQUEST_CODE = 159;
    private static boolean alarmAlreadySet = false;

    public static final String SETUP_ALARM_ACTION = "TreeNoteAlarmService.SETUP_ALARM_ACTION";

    public TreeNoteAlarmService() {
        super(TreeNoteAlarmService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (SETUP_ALARM_ACTION.equals(intent.getAction())) {
            if (!alarmAlreadySet) {

                AlarmManager alarmManager
                        = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        getTriggerAtMillis(),
                        INTERVAL_MILLIS,
                        getPendingIntent()
                );

                Log.i(TAG, "Alarm successfully set");
                alarmAlreadySet = true;
            } else {
                Log.i(TAG, "Alarm was already set");
            }
        } else {
            Log.w(TAG, "unknmown Action: " + intent.getAction());
        }
    }

    private PendingIntent getPendingIntent() {
        Intent showNotificationIntent = new Intent(this, TreeNodeNotificationReceiver.class);
        showNotificationIntent.setAction(TreeNodeNotificationReceiver.CREATE_NOTIFICATION_ACTION);
        return PendingIntent.getBroadcast(
                this,
                PENDING_INTENT_REQUEST_CODE,
                showNotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private long getTriggerAtMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, TRIGGER_HOUR_OF_DAY);
        return calendar.getTimeInMillis();
    }
}
