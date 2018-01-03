package com.xfsi.batterychecker.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity implements MainFragment.OnFragmentInteractionListener,
        SubFragment.OnFragmentInteractionListener, LevelFragment.OnFragmentInteractionListener,
        TimeIntervalFragment.OnFragmentInteractionListener, NotifyFragment.OnFragmentInteractionListener,
        EmailInfoFragment.OnFragmentInteractionListener, TextInfoFragment.OnFragmentInteractionListener
    {

    private static final String TAG = "MainActivity";
    public static final boolean DEBUG = false;

   // private static final String PREFS_NAME = "com.xfsi.batterychecker";
    private static final String PREFS_NAME2 = "com.xfsi.batterychecker.app.NewAppWidget";

    public static final String INTERVAL_KEY = "interval";
    public static final String ISINTERVAL_KEY = "isinterval";

    public static final String N_NONE_KEY = "none";
    public static final String N_EMAILL_KEY = "email";
    public static final String N_TEXT_KEY = "text";
    public static final String N_NOTIFY_KEY = "notify";
    public static final String N_ALL_KEY = "all";

    public static boolean isEmail = false;       // email
    public static boolean isTextMsg = false;     // text message
    public static boolean isNotify = false;      // notify
    public static boolean isNone = true;         // none
    public static boolean isAll = false;         // All

    private static boolean isLevel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up actionbar
        android.app.ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.battery3_64x64);
        actionBar.setTitle("Battery Checker");
        actionBar.setSubtitle("Check and Notify");
        // backgroup_1.png is not working ???
       // actionBar.setBackgroundDrawable(R.drawable.backgroup_1);

        setContentView(R.layout.activity_main);

        // from here I get the access to resource Ids from MainFragment
        TextView batteryNow = (TextView)findViewById(R.id.batteryNow);
        float batteryValue = BatteryAppWidget.getBatteryLevel(this);
        int batteryValueInt = (int)batteryValue;
        String batteryNowStr = "The Battery level is: " + String.valueOf(batteryValueInt) + "%";
        batteryNow.setText(batteryNowStr);

        if (null == savedInstanceState) {

            // I am not sure that I need this, I am going to put this in DEBUG tag
            // It is the first time, retrieve some values from share prefs
            if (DEBUG) {
                SharedPreferences prefs = getSharedPreferences(SPKeys.PREFS_NAME, 0);
                boolean emailCB = prefs.getBoolean(SPKeys.EMAILCB_KEY, false);
                String email = prefs.getString(SPKeys.EMAILET_KEY, null);
                String password = prefs.getString(SPKeys.PWET_KEY, null);
                String recipients = prefs.getString(SPKeys.RECIPIENTS_KEY, null);

                boolean textCB = prefs.getBoolean(SPKeys.TEXTCB_KEY, false);
                String phoneNums = prefs.getString(SPKeys.PHONENUMS_KEY, null);

                boolean notifyCB = prefs.getBoolean(SPKeys.NOTIFY_KEY, false);


                Log.d(TAG, "saved==null, emailCB is " + emailCB + ", email is " + email
                        + ", password is " + password + ", recipients are " + recipients
                        + ", textCB is " + textCB + ", phoneNums are " + phoneNums
                        + ", notifyCB is " + notifyCB);
            }

            SubFragment subFragment = SubFragment.newInstance(null, null);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.mainContainer, subFragment, "SubFragment_Tag");
            // no save parent on stack, bc we don't want backbtn to blank page
            ft.commit();
        }
        if (DEBUG) {Log.d(TAG, "Finished create Home view");}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // K not sure about Home/Up button, I don't have it. ??

        // let change this to switch statements
        switch (item.getItemId()) {
            case R.id.emailInfo:
                showEmailInfo();
                break;
            case R.id.textInfo:
                showTextInfo();
                break;
           // case R.id.notify_settings:
            //    showNotifyMethods();
            //    break;
            default:
                break;
        }
        return true;
    }

   /* private void showNotifyMethods() {
        Intent intent = new Intent(getApplicationContext(), NotifyMethodsActivity.class);
        // K not sure that I need these flags
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }*/

    private void showEmailInfo() {
        // create EmailInfoFragment
        EmailInfoFragment emailInfoFragment = EmailInfoFragment.newInstance(null, null);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.mainContainer, emailInfoFragment, "EmailInfo_Tag");
        ft.addToBackStack("EmailInfo_Tag");
        //ft.addToBackStack("SubFragment_Tag"); does not seems work to go back to particular tag
        // I may look into this later
        ft.commit();
    }

    private void showTextInfo() {
        // create TextInfoFragment
        TextInfoFragment textInfoFragment = TextInfoFragment.newInstance(null, null);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.mainContainer, textInfoFragment, "TextInfo_tag");
        ft.addToBackStack("TextInfo_tag");
        ft.commit();
    }

    @Override
   public void onFragmentInteraction(Uri uri) {
   // public void onFragmentInteraction(Bundle bundle) {
    }

    //@Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }
}
