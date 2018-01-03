package com.xfsi.batterychecker.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link BatteryAppWidgetConfigureActivity NewAppWidgetConfigureActivity}
 */
public class BatteryAppWidget extends AppWidgetProvider {

    private static final String TAG = "BatteryAppWidget";
    public static final boolean DEBUG = false;

    //private static final String PREFS_NAME = "com.xfsi.batterychecker";

    // this String is use in onReceive(), to make sure we only handle this unique Action.
    public static final String ACTION_EXPLICIT_UPDATE = "com.xfsi.batterychecker.action.EXPLICIT_UPDATE";
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    //public static final String SPPHONE_NUMBER_KEY = "phone_number_key";     // its value as string

    private static Context mContext;   // need it to pass to Asynctask in send mail

    // to hold current widget id to use in share prefs??
   // private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public void onReceive(@NonNull Context context, @NonNull Intent intent) {

        mContext = context;

        String action = intent.getAction();
       //LogHelper.log("onReceive action= kieu=" + action);
        if (DEBUG) {
            Log.i(TAG, "onReceive action= " +action);
        }

        // we only handle our own action = "com.xfsi.batterychecker.action.EXPLICIT_UPDATE"
        if (BatteryAppWidget.ACTION_EXPLICIT_UPDATE.equalsIgnoreCase(action)
                || BatteryAppWidget.ACTION_BOOT_COMPLETED.equalsIgnoreCase(action)) {
            doExplicitUpdate(context, intent);

            // if BOOT_COMPLETED then I need to re start alarm
            if (BatteryAppWidget.ACTION_BOOT_COMPLETED.equalsIgnoreCase(action) ) {
                // didn't get in here ???
                int interval;
                // need to get the mInterval from shar pref
                // get level from share pref
                SharedPreferences prefs = context.getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE);
                interval = prefs.getInt(SPKeys.INTERVAL_KEY, 12);

                // I got this message in Android Device Monitor
               // LogHelper.log("onReceive interval= kieu3=" + interval);

                // set the alarm at interval value, mInterval ???
                if (interval != 12) {
                    // I think it is working
                    //LogHelper.log("onReceive interval= kieu2=" + interval);
                    AlarmUtil.setAlarmAtInterval(interval, context);
                }

            }
        } else {
            super.onReceive(context, intent);
        }
    }

    private void doExplicitUpdate(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName appWidgetComponentName = new ComponentName(context, BatteryAppWidget.class);

        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        /* if you find one appWidgetId, then create a new int[]  appWidgetIds and put
        one app widget id inside it; otherwise, just assign all app Widget ids to int[] appWidgetIds
	    */
        int[] appWidgetIds = appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID ?
                appWidgetManager.getAppWidgetIds(appWidgetComponentName) : new int[]{appWidgetId};

        if (appWidgetIds != null && appWidgetIds.length > 0)
            onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //LogHelper.log(String.format("Widget ID Count:%d", appWidgetIds.length));
        // There may be multiple widgets active, so update all of them
       // final int N = appWidgetIds.length;
        //for (int i=0; i<N; i++) {
        // K change to for-each loop for Git warning error
        for (int appWidgetId: appWidgetIds) {
            //int appWidgetId = appWidgetIds[i];
            // It will log info to android.util.Log system
           //LogHelper.log(String.format("Widget ID:%d", appWidgetId));

            // we call updateAppWidget(..) for each widget instance
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        // we want to delete all the instance of this widget.
        //final int N = appWidgetIds.length;
        // for (int i=0; i<N; i++) {
        for (int appWidgetId : appWidgetIds) {
            // because this I have to declare static many methods
            // in BatteryAppWidgetConfigureActivity
            BatteryAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
         }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    //K static because it needs to access by the system??
    // this is my updateAppWidget, this will call appWidgetManager.appWidgetUpdate()
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        // I got the widgetId here,  save it in mAppWidgetId, then use it for share prefs
        //mAppWidgetId = appWidgetId;

        // get def title = "Battery Checker Widget"
        CharSequence widgetText = BatteryAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        // display battery icon + "Battery Checker", after "Done"
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fragment_show_image);

        views.setTextViewText(R.id.widgetName, widgetText);

        //Intent btnIntent = new Intent(context, ShowImageActivity.class);
        Intent intent = new Intent(context, MainActivity.class);

        //K originally I had two buttons on the widget itself and display battery level
        // now I just have battery icon + "Battery Checker"
        // when user touch the icon => main activity
        intent.setAction("com.xfsi.batterychecker.action.GOTO_MAIN_ACTIVITY");
        // btnIntent.setAction("com.xfsi.batterychecker.action.SHOW_IMAGE");

        // create pi for main activity
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // layout id of the root layout associate with this RemoteViews
        // int remoteLId = views.getLayoutId();  // I don't use this

        // when user touch the icon => main activity
        int batteryIcon = R.id.batteryIcon;
        views.setOnClickPendingIntent(batteryIcon, pi);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // get the current battery level
        float batlev = getBatteryLevel(context);
        //String batlevTx = String.valueOf(batlev);

        // get level from share pref
        SharedPreferences prefs = context.getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE);
        // first time share prefs has nothing in it
        int level;
        try {
            level = prefs.getInt(SPKeys.LEVEL_KEY, 40);
        } catch (Exception e) {
            level = 40;
        }

        // check the level is low, then send notification, email
        if ( batlev < level ) {

            // need to get the checkboxes value from share prefs
            boolean emailCB = prefs.getBoolean(SPKeys.EMAILCB_KEY, false);
            boolean textCB = prefs.getBoolean(SPKeys.TEXTCB_KEY, false);
            boolean notifyCB = prefs.getBoolean(SPKeys.NOTIFY_KEY, false);
            long notifyTime = prefs.getLong(SPKeys.NOTIFY_TIME_KEY, 0);
            long currentTime = System.currentTimeMillis();

            // if currentTime should be greater 30 minutes than previous time in share pref
            // 30 mins = 30* 60*1000 millis = 1,800,000 millisSeconds
            if ( currentTime > notifyTime + 1800000) {

                // save currentTime to share pref
                SharedPreferences.Editor prefsED = context.getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE).edit();
                prefsED.putLong(SPKeys.NOTIFY_TIME_KEY, currentTime);
                prefsED.commit();

                // create long message for email and text
                String message = createMessage(context, (int) batlev, appWidgetId);

                if (notifyCB) {
                    // need to create short message for notify
                    String notifymsg = "Battery Level is: " + (int)batlev + "%";
                    generateNotification(context, notifymsg, (int) batlev);
                }
                if (textCB) {
                    sendSms(context, message);
                }
                if (emailCB) {
                    sendMail(message);
                }
                if (DEBUG) {
                    Log.d(TAG, "updateAppWidget(): " + "Notify: " + notifyCB + ", Text: " + textCB
                            + ", Email: " + emailCB);
                }
            }
        }
    }

    // this has to static because static void updateAppWidget(..)
    private static void sendSms(Context context,String message) {

        SharedPreferences prefs = context.getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE);
        String phoneNumsStr = prefs.getString(SPKeys.PHONENUMS_KEY, null);

        // phoneNums is not null
       // if ( (null != phoneNumsStr) || (phoneNumsStr.length() >0) ){
        if (TextUtils.isEmpty(phoneNumsStr)){
            SendTextBG sendTBG = new SendTextBG(context, phoneNumsStr, message);
            sendTBG.execute();
            if (DEBUG){
                Log.d(TAG, "sendText: " + "phoneNums are " + phoneNumsStr
                        + ", message is: " + message);
            }
        }
    }

    // this has to static because static void updateAppWidget(..)
    private static void sendMail(String message) {
        SendMailBG sendMBG = new SendMailBG(mContext, message);
        sendMBG.execute();
        if (DEBUG){
            Log.d(TAG, "sendMail: " + "Message Body: " + message);
        }
    }

    // create message for email or text
    // this has to be static because update() is static
    private static String createMessage(Context context, int batlev, int appWidgetId) {
        // get system current times
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String sysTime = dateFormat.format(date);

        // need to get the current level and interval notify methods to create the message
        SharedPreferences prefs = context.getSharedPreferences(SPKeys.PREFS_NAME, 0);

        // get widget name
        CharSequence widgetName = BatteryAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        int level = prefs.getInt(SPKeys.LEVEL_KEY, 40);
        int interval = prefs.getInt(SPKeys.INTERVAL_KEY, 12);
        boolean email = prefs.getBoolean(SPKeys.EMAILCB_KEY, false);
        boolean text = prefs.getBoolean(SPKeys.TEXTCB_KEY, false);
        boolean notify = prefs.getBoolean(SPKeys.NOTIFY_KEY, false);

         /*String message = sysTime + ": Your android phone battery level is "
                + batlev + "%"
                + ". Current setting: Level is " + level
                + "%, interval is " +interval
                + " hrs. Notify: email is " + email + ", text is "
                + text + ", notify is " + notify + "."
                + " Widget Name is " + widgetName;*/

       // return message;
        // I have to do this for Git give me a warning
        return sysTime + ": Your android phone battery level is "
                + batlev + "%"
                + ". Current setting: Level is " + level
                + "%, interval is " +interval
                + " hrs. Notify: email is " + email + ", text is "
                + text + ", notify is " + notify + "."
                + " Widget Name is " + widgetName;
    }

    // this has to static because static void updateAppWidget(..)
    private static void generateNotification(Context context, String message, int batlev) {
        int smallIcon = R.drawable.battery_32x32;
        int largeIcon = R.drawable.battery3_64x64;

        Resources res = context.getResources();
        Bitmap largeBitmap = BitmapFactory.decodeResource(res, largeIcon);
        long when = System.currentTimeMillis();     // use in create notification object

        NotificationManager notificationMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // touch the battery icon => main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("content", message);    // I don't think I use this
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // create another PendingIntent for "Check Level" button
        Intent intent2 = new Intent(context, CheckLevelActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi2 = PendingIntent.getActivity(context, 0, intent2, 0);

        // create another PendingIntent for "Time interval" button
        Intent intent3 = new Intent(context, TimeIntervalActivity.class);
        intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi3 = PendingIntent.getActivity(context, 0, intent3, 0);

        //Notification notification = new Notification(icon, message, when);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        //int largeBatteryIcon = R.drawable.battery3_64x64;
        int setLevelIcon = R.drawable.level_40_40;
        int timeIcon = R.drawable.h6_64;

        String title = context.getString(R.string.app_name);
        mBuilder.setContentTitle(title)    // first row
                .setSmallIcon(smallIcon)   // right bottom corner
                .setLargeIcon(largeBitmap)  // left battery image
                .setWhen(when)              // current time
                .setContentText("The battery level is " + batlev + "%")   // second row
                .setTicker(message)    // text when first arrives
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .addAction(setLevelIcon, "Level", pi2)
                .addAction(timeIcon, "Interval", pi3)
                .setAutoCancel(true)
                .setContentIntent(pi)   // goto MainActivity
                .setPriority(Notification.PRIORITY_HIGH);

        Notification notification = mBuilder.build();
        /*
        K id=0 is means any notification 0 will overwritte the previous
        id= 0 notification, there should only be one notification id = 0
         */
        notificationMgr.notify(0, notification);
        if (DEBUG){
            Log.d(TAG, "generateNotification() is done");
        }
    }

    public static void errorNotify(Context context, String message, int id) {
        int smallIcon = R.drawable.battery_32x32;
        int largeIcon = R.drawable.battery3_64x64;

        Resources res = context.getResources();
        Bitmap largeBitmap = BitmapFactory.decodeResource(res, largeIcon);
        long when = System.currentTimeMillis();     // use in third row
        String whenStr = String.valueOf(when);

        NotificationManager notificationMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification notification = new Notification(icon, message, when);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

       // int largeBatteryIcon = R.drawable.battery3_64x64;
        //int setLevelIcon = R.drawable.level_40_40;
       // int timeIcon = R.drawable.h6_64;

        // touch the battery icon => main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("content", message);    // I don't think I use this
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String title = context.getString(R.string.app_name);
        mBuilder.setContentTitle(title)    // first row
                .setSmallIcon(smallIcon)   // right bottom corner
                .setLargeIcon(largeBitmap)  // left battery image
                .setWhen(when)              // current time
                .setTicker(message)    // text when first arrives
                .setContentText(message)    // second row
                .setSubText(whenStr)       // third row with time stamp
                .setAutoCancel(true)
                .setContentIntent(pi)   // goto MainActivity
                .setPriority(Notification.PRIORITY_HIGH);

        Notification notification = mBuilder.build();
        // K 0 => cancel all previous notification
        //notificationMgr.notify(0, notification);
        // don't cancel previous notification
        notificationMgr.notify(id, notification);
        if (DEBUG){
            Log.d(TAG, "errorNotify() is done");
        }
    }

    public static float getBatteryLevel(Context context) {

        // register receiver to get one time Battery change event
        // null means no receiver => now???
        // context.registerReceiver(..) work on real device, but not on emaulator
       // Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        /* K This works on emulator
            context.getApplicatonContext().registerReceiver(...)
            The Context that is passed to onReceive() is blocked from calling registerReceiver()
            even with a null BroadcastReceiver.
        */
        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            //Error checking that probably isn't needed but I added just in case.
            if (level == -1 || scale == -1) {
                return 50.0f;
            }
            return ((float) level / (float) scale) * 100.0f;
        } else {
            // K an other error checking, if system doesn't give you the battery info
            // then assume it is 50% battery level
            return 50.0f;
        }
    }
}


