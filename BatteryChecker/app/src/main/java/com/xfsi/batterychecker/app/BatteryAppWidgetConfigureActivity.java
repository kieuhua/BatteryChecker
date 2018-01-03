package com.xfsi.batterychecker.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;


/**
 * The configuration screen for the {@link BatteryAppWidgetConfigureActivity NewAppWidget} AppWidget.
 */

//K I need OnEditorActionListener here for "Done" on configuration activity
// K I decide that ETDialog is not appropriate for inputting the settings
//public class BatteryAppWidgetConfigureActivity extends Activity implements ETDialog.ETDialogListener,       TextView.OnEditorActionListener, ETDialog.OnFragmentInteractionListener {
public class BatteryAppWidgetConfigureActivity extends Activity {
    private static final String TAG = "BatteryAppWidgetConfigureActivity";
    public static final boolean DEBUG = false;

    // initialize with invalid widget ID
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    // get the EDText view of Widget ID
    EditText mAppWidgetET;

    // I need all in global, so I can use them in listeners
    CheckBox mNotifyChkBx;

    // RadioGroup mLevelRadioG, int mSelectedLevelRBId, int mLevelValue
    RadioGroup mLevelRadioG;
    EditText mLevelET;
    int mSelectedLevelRBId;
    int mLevel = 40;

    // RadioGroup mIntervalRadioG, int mSelectedIntervalRBId, int mIntervalValue
    RadioGroup mIntervalRadioG;
    EditText mIntervalET;
    int mSelectedIntervalRBId;
    int mInterval = 12;

    // K always need default constructor
    public BatteryAppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // if user presses the back button; other then hit "Done" button to get out of Config activity
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement.
        // this will call BatteryAppWidget.onDelete(..) to run
        setResult(RESULT_CANCELED);

        setContentView(R.layout.batterychecker_widget_configure);

        // find the level radio group
        mLevelRadioG = (RadioGroup) findViewById(R.id.radioGLevel);
        mLevelET = (EditText) findViewById(R.id.levelET);

        // find the interval radio group
        mIntervalRadioG = (RadioGroup) findViewById(R.id.radioGInterval);
        mIntervalET = (EditText) findViewById(R.id.intervalET);

        mNotifyChkBx = (CheckBox) findViewById(R.id.notifyChkBx);

        mAppWidgetET = (EditText)findViewById(R.id.appwidgetET);

        // set done button click listener
        findViewById(R.id.donebtn).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        // 1st time
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        // this is where appwidget id come from the intent got the widget ID
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // I need widget id to able to access widget name,
        String widgetText = loadTitlePref(BatteryAppWidgetConfigureActivity.this, mAppWidgetId);
       // mAppWidgetET.setText(loadTitlePref(BatteryAppWidgetConfigureActivity.this, mAppWidgetId));
       // this is the default widget name
        mAppWidgetET.setText(widgetText);

        // Initialize or restore the values
        initialOrRestore(icicle);
    }

    // Initialize the values from share prefs on the first time,
    // or restore the values from Bundle outState
    private void initialOrRestore(Bundle icicle) {
        if (icicle != null) {
            // check the Level radio button
            mLevelRadioG.check(icicle.getInt(SPKeys.LEVEL_RBID_KEY, R.id.radio_l40));
            mLevelET.setText(icicle.getString(SPKeys.LEVELET_KEY, "40"));
            // check the Interval radio button
            mIntervalRadioG.check(icicle.getInt(SPKeys.INTERVAL_RBID_KEY, R.id.radio_h12));
            mIntervalET.setText(icicle.getString(SPKeys.INTERVALET_KEY, "12"));

            mNotifyChkBx.setChecked(icicle.getBoolean(SPKeys.NOTIFY_KEY));

           // mAppWidgetET.setText(icicle.getString(APPWIDGET_KEY));
            String widgetName = icicle.getString(SPKeys.APPWIDGET_KEY);
            mAppWidgetET.setText(widgetName);
        } else {
            SharedPreferences prefs = getSharedPreferences(SPKeys.PREFS_NAME, 0);

            int level = prefs.getInt(SPKeys.LEVEL_KEY, 40);  // right here ????
            int interval = prefs.getInt(SPKeys.INTERVAL_KEY, 12); // here too???

            boolean notifycb = prefs.getBoolean(SPKeys.NOTIFY_KEY, false);

            // need switch statement to check the level radio button by int level
            if (level > 0 ) { checkLevelRB(level); }
            // need switch statement to check the interval radio button by int interval
            if (interval > 0 ) { checkIntervalRB(interval); }

            // android notify
            if (notifycb) { mNotifyChkBx.setChecked(true); }
        }
    }

    // handle "Done" button on Configure activity
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Context context = BatteryAppWidgetConfigureActivity.this;

            String widgetText = mAppWidgetET.getText().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            BatteryAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

            // cancel all alarms, and save all values to share prefs
            verifyNsave(context);

            // to be used in other parts of the widget
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // cancel all alarms, and save all values to share prefs
    private void verifyNsave(Context context) {
        SharedPreferences.Editor prefsED = getSharedPreferences(SPKeys.PREFS_NAME, 0).edit();

        // get the selected level Radio button, and save its integer value in mLevel
        selectedLevel();

        // get the selected interval Radio button, and save its integer value in mInterval
        selectedInterval();

        // cancel all other alarms, and set the selected interval
        resetAlarm();

        // save the values of Level and Interval
        prefsED.putInt(SPKeys.LEVEL_KEY, mLevel);
        prefsED.putInt(SPKeys.INTERVAL_KEY, mInterval);
        prefsED.putBoolean(SPKeys.NOTIFY_KEY, mNotifyChkBx.isChecked());

        prefsED.putString(SPKeys.APPWIDGET_KEY, mAppWidgetET.getText().toString());
        prefsED.commit();

        if (DEBUG) {
            // I don't want to log password
            Log.d(TAG,
                    "verifyNsave(): "
                    + ", mLevel is " + mLevel
                    + ", mInterval is " + mInterval
                    + ", notify is " + mNotifyChkBx.isChecked()
                    + ", appwidget is " + mAppWidgetET.getText().toString()
            );
        }
    }

    // get the selected level Radio button, and save its integer value in mLevel
    private void selectedLevel() {
        // get selected level radiobtn id
        mSelectedLevelRBId = mLevelRadioG.getCheckedRadioButtonId();
        switch (mSelectedLevelRBId) {
            case R.id.radio_l20:
                mLevel = 20;
                break;
            case R.id.radio_l40:
                mLevel = 40;
                break;
            case R.id.radio_l60:
                mLevel = 60;
                break;
            case R.id.radio_custom:
                mLevel = Integer.valueOf(mLevelET.getText().toString());
                break;
            default:
                mLevel = 40;
                break;
        }
    }
    // with the level value, check the correct level radio button and set level ET
    private void checkLevelRB(int level) {

        RadioGroup mRadioG = (RadioGroup) findViewById(R.id.radioGLevel);
        switch (level) {
            case 20:
                mRadioG.check(R.id.radio_l20);
                mLevelET.setText("20");
                break;
            case 40:
                mRadioG.check(R.id.radio_l40);
                mLevelET.setText("40");
                break;
            case 60:
                mRadioG.check(R.id.radio_l60);
                mLevelET.setText("60");
                break;
            default:
                if (level > 0 ) {
                    mRadioG.check(R.id.radio_custom);
                    mLevelET.setText(String.valueOf(level));
                }
                break;
        }
    }

    // get the selected interval Radio button, and save its integer value in mInterval
    private void selectedInterval() {
        mSelectedIntervalRBId = mIntervalRadioG.getCheckedRadioButtonId();
        switch (mSelectedIntervalRBId) {
            case R.id.radio_h4:
                mInterval = 4;
                break;
            case R.id.radio_h8:
                mInterval = 8;
                break;
            case R.id.radio_h12:
                mInterval = 12;
                break;
            case R.id.radio_interval_custom:
                mInterval = Integer.valueOf(mIntervalET.getText().toString());
                break;
            default:
                mInterval = 12;
                break;
        }
    }
    // with the interval value, check the correct interval radio button and set interval ET
    private void checkIntervalRB(int interval) {
        RadioGroup mRadioG = (RadioGroup) findViewById(R.id.radioGInterval);
        switch (interval) {
            case 4:
                mRadioG.check(R.id.radio_h4);
                mIntervalET.setText("4");
                break;
            case 8:
                mRadioG.check(R.id.radio_h8);
                mIntervalET.setText("8");
                break;
            case 12:
                mRadioG.check(R.id.radio_h12);
                mIntervalET.setText("12");
                break;
            default:
                if (interval > 0 ) {
                    mRadioG.check(R.id.radio_interval_custom);
                    mIntervalET.setText(String.valueOf(interval));
                }
                break;
        }
    }


    // cancel all other alarms related to ACTION_EXPLICIT_UPDATE
    private void resetAlarm() {
        // cancel all other alarms, for 12 hrs is default widget update
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // create Intent
        Intent intent = new Intent(this, BatteryAppWidget.class);
        intent.setAction(AlarmUtil.ACTION_EXPLICIT_UPDATE);
        // create PendingIntent
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.cancel(pi);

        // set the alarm at interval value, mInterval ???
        if (mInterval != 12) {
            AlarmUtil.setAlarmAtInterval(mInterval, getApplicationContext());
        }
    }

    static void saveTitlePref(Context context, int appWidgetId, String text) {
    //void saveTitlePref(Context context, int appWidgetId, String text) {
        // appWidgetId = 376, 377, 378
        // text = "Battery Checker Widget" I need to change it to appWidgetId
        SharedPreferences.Editor prefs = context.getSharedPreferences(SPKeys.PREFS_NAME, 0).edit();
        prefs.putString(SPKeys.APPWIDGETID_KEY+ appWidgetId, text);
        prefs.commit();
    }

    // get a string from appWidgetId
    static String loadTitlePref(Context context, int appWidgetId) {
   // String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(SPKeys.PREFS_NAME, 0);
        String titleValue = prefs.getString(SPKeys.APPWIDGETID_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            // this appear in as Widget Id EditText field
            // and it gets the text from R.string.appwidget_text
            String out = titleValue;
            return context.getString(R.string.appwidget_text);
        }
    }

    // It is called by onDeleted(..) in BatteryAppWidget.java
    static void deleteTitlePref(Context context, int appWidgetId) {
    //void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(SPKeys.PREFS_NAME, 0).edit();
        prefs.remove(SPKeys.APPWIDGETID_KEY + appWidgetId);
        prefs.commit();
    }

    // delete all the share prefs in PREFS_NAME
    // It is called by onDeleted(..) in BatteryAppWidget.java
    static void deletePrefsName(Context context) {
        SharedPreferences.Editor prefsED = context.getSharedPreferences(SPKeys.PREFS_NAME, 0).edit();
        prefsED.remove(SPKeys.LEVEL_KEY);
        prefsED.remove(SPKeys.INTERVAL_KEY);

        prefsED.remove(SPKeys.NOTIFY_KEY);
        prefsED.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // K I assume that there is always a selected Radio button in Level and interval
        // because, I set them by default in the layout

        // in config change, I need to save selected level radio button
        // if it is custom, then I also need to save the value
        // need two m variables -
        // RadioGroup   saveLevelmLevelRadioG, int mSelectedLevelRBId, int mLevelValue
        mSelectedLevelRBId = mLevelRadioG.getCheckedRadioButtonId();
        // it is impossible to uncheck radiobutton, since I always checked a default one
        // so this should not be 0
        outState.putInt(SPKeys.LEVEL_RBID_KEY, mSelectedLevelRBId);
        outState.putString(SPKeys.LEVELET_KEY, mIntervalET.getText().toString());

        // in config change, I need to save selected interval radio button
        // if it is custom, then I also need to save the value
        // RadioGroup mIntervalRadioG, int mSelectedIntervalRBId, int mIntervalValue
        mSelectedIntervalRBId = mIntervalRadioG.getCheckedRadioButtonId();
        outState.putInt(SPKeys.INTERVAL_RBID_KEY, mSelectedIntervalRBId);
        outState.putString(SPKeys.INTERVALET_KEY, mIntervalET.getText().toString());

        outState.putBoolean(SPKeys.NOTIFY_KEY, mNotifyChkBx.isChecked());

       // outState.putString(APPWIDGET_KEY, mAppWidgetET.getText().toString());
        String widgetName = mAppWidgetET.getText().toString();
        outState.putString(SPKeys.APPWIDGET_KEY, widgetName);
    }
}



