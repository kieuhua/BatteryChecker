package com.xfsi.batterychecker.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by local-kieu on 6/25/14.
 */
public class SendMailBG extends AsyncTask<Void, Void, String> {

    private static final String TAG = "SendMailBG";
    public static final boolean DEBUG = false;

    private static final int NID = 20;

    //protected static final String PREFS_NAME = "com.xfsi.batterychecker";
    String mBody; // this is message body
    Context mContext;    // need the application context for calling Share pref

    SendMailBG(Context context,String body){
        mContext = context;
        mBody = body;
    }

    @Override
    protected String doInBackground(Void... params) {
        int nId = NID;   // unique Android notification Id for Email sending.

        try{
            // get the sender email, password and recipients from Share pref
            SharedPreferences prefs = mContext.getSharedPreferences(SPKeys.PREFS_NAME, 0);
            String senderEmail = prefs.getString(SPKeys.EMAILET_KEY, null);
            String senderPw = prefs.getString(SPKeys.PWET_KEY, null);
            String recipients = prefs.getString(SPKeys.RECIPIENTS_KEY, null);

            // should check the sender is valid "@gmail.com",for share prefs may be corrupted.
            // if not gmail send nofity

            // for testing sender email
            //senderEmail = "TestInvalidSender";
            //senderEmail = null;
            if (TextUtils.isEmpty(senderEmail)) {
                BatteryAppWidget.errorNotify(mContext, "Sender Email is null.", nId++);
                if ( DEBUG) {Log.e(TAG, "Sender Email is null." );}
                return "Email Not Sent";
            }
            if ( !EmailInfoFragment.senderCheck(senderEmail)) {
                // send error notify;
                BatteryAppWidget.errorNotify(mContext, "Invalid Sender Email: " + senderEmail + ".", nId++);
                if ( DEBUG) {Log.e(TAG, "Invalid Sender Email: " + senderEmail + "." );}
                return "Email Not Sent";
            }

            // for testing sender password
            //senderPw = "kieu";
            //senderPw = null;
            if (TextUtils.isEmpty(senderPw)) {
                BatteryAppWidget.errorNotify(mContext, "Sender Password is null.", nId++);
                if ( DEBUG) {Log.e(TAG, "Sender Password is null." );}
                return "Email Not Sent";
            }
            if ( !EmailInfoFragment.passwordCheck(senderPw)) {
                // send error notify;
                BatteryAppWidget.errorNotify(mContext, "Invalid Sender Password.", nId++);
                if (DEBUG) { Log.e(TAG, "Invalid Sender password." ); }
                return "Email Not Sent";
            }


            // for testing recipients
            // recipients = null;
            // recipients = "kieu";
            if (TextUtils.isEmpty(recipients)) {
                BatteryAppWidget.errorNotify(mContext, "Recipient Email is null.", nId++);
                if ( DEBUG) {Log.e(TAG, "Recipient Email is null." );}
                return "Email Not Sent";
            }

            ContentValues result = EmailInfoFragment.recipientsCheck(recipients);
            String vRecipients = null;
            String invRecipients = null;

            // check result can be null, if recipients is null
            if ( null != result) {
                vRecipients = result.getAsString(EmailInfoFragment.VALID_RECIPIENTS_KEY);
                // I decide to not care about invalid recipients, since the sender will get undeliveried mail.
                invRecipients = result.getAsString(EmailInfoFragment.INVALID_RECIPIENTS_KEY);
            }

            if (TextUtils.isEmpty(vRecipients)) {
                // send error notify;
                BatteryAppWidget.errorNotify(mContext, "No valid recipient Email.", nId++);
                if (DEBUG) { Log.e(TAG, "No valid recipient Email." );}
                return "Email Not Sent";
            }

            GMailSender sender = new GMailSender(mContext,senderEmail, senderPw);

            // sendMail( subject, message, from, to
           // sender.sendMail( "Battery level of your Android device", mBody, "kieu.hua@gmail.com",
            //        vRecipients);
            sender.sendMail( "Battery level of your Android device", mBody, senderEmail,
                    vRecipients);
            // I don't want to log user password
            if (DEBUG){ Log.d(TAG, "doInBackground(), after sender.sendMail(), senderEmail is "
                    + senderEmail + ", recipients are " + recipients);
            }
        }
        catch (Exception e) {
            // I got IllegalStateExeption: Not connected,
            // when sender email or password is invalid
            // the exception here is too general, I need to get
            // the exception in GMailSender instead
            // Not sure how I can test this ???
            // I need put in the time stamp
            BatteryAppWidget.errorNotify(mContext, "Mail sender failed, email Not sent", nId++);
            //need to resend this mail ??? I could create infinite loop => no
            e.printStackTrace();
            if ( DEBUG) { Log.e(TAG, e.getMessage(), e); }
            return "Email Not Sent";
        }
        return  "Email Sent";
    }
}
