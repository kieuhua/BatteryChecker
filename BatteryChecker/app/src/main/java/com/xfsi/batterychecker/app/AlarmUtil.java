package com.xfsi.batterychecker.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by local-kieu on 7/14/14.
 */
public class AlarmUtil {

    public static final String ACTION_EXPLICIT_UPDATE = "com.xfsi.batterychecker.action.EXPLICIT_UPDATE";

    // cancel previous alarm, and set the new alarm interval
    // interval is hours
    public static void setAlarmAtInterval(int interval, Context context) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // create Intent
        Intent intent = new Intent(context, BatteryAppWidget.class);
        intent.setAction(ACTION_EXPLICIT_UPDATE);
        // create PendingIntent
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        // cancel all alarm related to ACTION_EXPLICIT_UPDATE
        am.cancel(pi);

        // get current time and time interval in Millis
        // restart the alarm
        long currentTM = System.currentTimeMillis();
        long intervalM = interval * 60 * 60000;    // 60 mins * 1 min

        // create new pending intent with the same intent
        pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        // start alarm with new interval time
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, currentTM + intervalM, intervalM, pi);
    }
}
