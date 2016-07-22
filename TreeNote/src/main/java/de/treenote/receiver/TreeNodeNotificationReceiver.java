package de.treenote.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import de.treenote.R;
import de.treenote.activities.TreeNoteMainActivity;
import de.treenote.fragments.TreeNotePreferenceFragment;
import de.treenote.util.CurrentFilterHolder;
import de.treenote.util.TreeNoteDataHolder;

public class TreeNodeNotificationReceiver extends WakefulBroadcastReceiver {

    public static final String CREATE_NOTIFICATION_ACTION = "TreeNodeNotificationReceiver.CREATE_NOTIFICATION_ACTION";
    public static final int PENDING_INTENT_REQUEST_CODE = 147;

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(context);

        boolean showNotification = sharedPreferences.getBoolean(TreeNotePreferenceFragment.ENABLE_NOTIFICATIONS, false);

        if (showNotification && CREATE_NOTIFICATION_ACTION.equals(intent.getAction())) {

            CurrentFilterHolder.currentFilter = CurrentFilterHolder.Filter.Today;

            if (TreeNoteDataHolder.getTreeNodeRoot().isVisibleForCurrentSearch()) {
                // at least one item was scheduled for today

                Intent openToDosForTodayIntent = new Intent(context, TreeNoteMainActivity.class);
                openToDosForTodayIntent.putExtra(TreeNoteMainActivity.NAVIGATION_ITEM_ID, R.id.action_today);
                intent.setAction(TreeNoteMainActivity.SHOW_TASKS_FOR_TODAY_ACTION);

                PendingIntent openToDosForTodayPendingIntent = PendingIntent.getActivity(
                        context,
                        PENDING_INTENT_REQUEST_CODE,
                        openToDosForTodayIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


                Notification notification = new Notification.Builder(context)
                        .setTicker(context.getString(R.string.notification_text))
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setAutoCancel(true)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.notification_text))
                        .setContentIntent(openToDosForTodayPendingIntent)
                        .build();

                // Get the NotificationManager
                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                // Pass the Notification to the NotificationManager:
                mNotificationManager.notify(0, notification);
            }
        }
    }
}
