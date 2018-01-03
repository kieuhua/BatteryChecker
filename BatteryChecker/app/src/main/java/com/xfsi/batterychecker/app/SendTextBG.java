package com.xfsi.batterychecker.app;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by local-kieu on 8/7/14.
 */
public class SendTextBG extends AsyncTask<Void, Void, String> {

    private static final String TAG = "SendMailBG";
    public static final boolean DEBUG = false;
    private static final int NID = 30;

    String mMessage;
    String mPhoneNumsStr;
    Context mContext;

    SendTextBG(Context context, String phoneNumsStr, String message) {
        mMessage = message;
        mContext = context;
        mPhoneNumsStr = phoneNumsStr;
    }

    @Override
    protected String doInBackground(Void... params) {
        StringBuffer errorMsg = new StringBuffer();     // to contains errors
        int nId = NID;       // Text message notify error start at NID id

        try {
            SmsManager smsManager = SmsManager.getDefault();
            // need to test mMessage for null; othewise I got IllegalArgumentException
            ArrayList<String> parts = smsManager.divideMessage(mMessage);

            String[] phoneNums = mPhoneNumsStr.split(",");

            if (phoneNums.length > 0) {

                // K I still need to check them here for info may be corrupted
                // Limit to 5 text phone numbers, so this will not go to infinity
                for (int i = 0; (i < phoneNums.length && i < 5); i++) {

                    try {
                        String phone = phoneNums[i];
                        String  validPhone = TextInfoFragment.checkPhone(phone);

                        if (TextUtils.isEmpty(validPhone) ) {
                            errorMsg.append( '"' + phone + '"' + " is invalid, ");
                            continue; // skip this number
                        } else {
                            smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
                        }
                    } catch (IllegalArgumentException e) {
                        // IllegalArgumentException if destinationAddress or data empty
                        errorMsg.append("Texting failed to " + '"' + phoneNums[i]+ '"' + ", ");
                    }
                }
                // this is less important error, so I put it at the end of error message
                // if more than 5 text phone numbers, then send notify
                if (phoneNums.length > 5) {
                    errorMsg.append("Five plus phones, ");
                }
            }
        } catch (IllegalArgumentException e) {

            // I am not sure if one phone is not sent what happend to here ????

            e.printStackTrace();
            if (DEBUG) {
                Log.d(TAG, "doInBackground(), after "
                        + "smsManager.sendMultipartTextMessage(): phoneNums are  " + mPhoneNumsStr
                        + ",  messages are " + mMessage);
                Log.e(TAG, e.getMessage(), e);
            }
            // Take out the last ", "
            String errorMsgStr = errorMsg.toString();
            errorMsgStr = errorMsgStr.substring(0, errorMsg.length() - 2);
            // add " ." to the end and last info
            errorMsgStr += ".";

            // send only one error notify
            // Not sure how I can test this???
            BatteryAppWidget.errorNotify(mContext, "Texting Errors: " + errorMsgStr, nId++);
            return "Error in sending Text";
        }
        return "Text sent";
    }
}
