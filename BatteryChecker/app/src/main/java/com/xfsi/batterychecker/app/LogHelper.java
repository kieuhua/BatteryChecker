package com.xfsi.batterychecker.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by local-kieu on 5/27/14.
 */
public class LogHelper {
    public static final String LOG_TAG = "batterwidget";

    public static void log(String message){
        Log.d(LOG_TAG, message);
    }

    // I used this in BatteryAppWidget to debug BOOT_COMPLETED
    public static void logIntent(String label, Intent intent){
        log("------------------------ " + label + " -----------------------");
        String action = intent.getAction();
        log("action" + (action == null ? "<null>" : action));
        Bundle extras = intent.getExtras();
        if (extras != null){
            for (String key : extras.keySet()){
                log("extra key=" + key);
            }
        }
        log("--------------------------------------------------------------");
    }
}
