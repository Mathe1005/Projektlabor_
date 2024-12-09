package com.example.projektlabor;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationService extends BroadcastReceiver {
    private static final String CHANNEL_ID = "sport_events_channel";
    private static final String CHANNEL_NAME = "Sport Events";
    private static final SimpleDateFormat dateTimeFormat =
            new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventId = intent.getStringExtra("eventId");
        String eventName = intent.getStringExtra("eventName");
        String notificationType = intent.getStringExtra("notificationType");

        createNotification(context, eventName, getNotificationMessage(notificationType, eventName));
    }

    public static void scheduleEventNotification(Context context, EventActivity.Event event) {
        try {
            // Esemény időpontja
            String dateTimeString = event.eventTime + " " + event.startTime;
            Date eventDateTime = dateTimeFormat.parse(dateTimeString);

            if (eventDateTime != null) {
                // Esemény előtt 1 órával
                Calendar notificationTime = Calendar.getInstance();
                notificationTime.setTime(eventDateTime);
                notificationTime.add(Calendar.HOUR_OF_DAY, -1);

                if (notificationTime.getTimeInMillis() > System.currentTimeMillis()) {
                    scheduleNotification(context, event, notificationTime.getTimeInMillis());
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void scheduleNotification(Context context, EventActivity.Event event, long triggerTime) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("eventId", event.eventId);
        intent.putExtra("eventName", event.eventName);
        intent.putExtra("notificationType", "reminder");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                event.eventId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    public static void sendEventUpdateNotification(Context context, String eventName, String updateType) {
        createNotification(
                context,
                "Event Update",
                getNotificationMessage(updateType, eventName)
        );
    }

    private static String getNotificationMessage(String type, String eventName) {
        switch (type) {
            case "reminder":
                return "Your event '" + eventName + "' starts in 1 hour!";
            case "modified":
                return "The event '" + eventName + "' has been modified by the organizer.";
            case "cancelled":
                return "The event '" + eventName + "' has been cancelled by the organizer.";
            default:
                return "Update regarding event '" + eventName + "'";
        }
    }

    private static void createNotification(Context context, String title, String message) {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(title.hashCode(), builder.build());
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}