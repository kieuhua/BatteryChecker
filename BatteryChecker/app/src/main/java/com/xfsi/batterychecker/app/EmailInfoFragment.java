package com.xfsi.batterychecker.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EmailInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EmailInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EmailInfoFragment extends Fragment {
    private static final String TAG = "EmailInfoFragment";
    public static final boolean DEBUG = false;

    // These keys are for recipientsCheck to return two strings - valid and invalid recipients
    protected static final String VALID_RECIPIENTS_KEY = "valid_recipients_key";
    protected static final String INVALID_RECIPIENTS_KEY = "invalid_recipients_key";

    protected static final String EMAIL_REGEXP = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private CheckBox mEmailChkBx;
    private EditText mEmailET;
    private EditText mPwET;
    private EditText mRecipientsET;

    private View theView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EmailInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EmailInfoFragment newInstance(String param1, String param2) {
        EmailInfoFragment fragment = new EmailInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public EmailInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        theView = inflater.inflate(R.layout.fragment_email_info, container, false);

        //I need to set all the EditTexts here, so they can be used in onClickListener
        mEmailChkBx = (CheckBox)theView.findViewById(R.id.emailChkBx);
        mEmailET = (EditText)theView.findViewById(R.id.emailSender);
        mPwET = (EditText)theView.findViewById(R.id.pwSender);
        mRecipientsET = (EditText)theView.findViewById(R.id.recipients);

        // retrieve values from share prefs
        SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, 0);

        // get email info - sender email, pw, recipients restore them
        boolean emailCB = prefs.getBoolean(SPKeys.EMAILCB_KEY, false);
        String email = prefs.getString(SPKeys.EMAILET_KEY, null);
        String password = prefs.getString(SPKeys.PWET_KEY, null);
        String recipients = prefs.getString(SPKeys.RECIPIENTS_KEY, null);

        // mail checkbox needs to be enable all time in this fragment
        mEmailChkBx.setEnabled(true);

        // reset to the previous email settings
        if (emailCB) {
            mEmailChkBx.setChecked(true);
        }
        mEmailET.setText(email);
        mPwET.setText(password);
        mRecipientsET.setText(recipients);
        if (DEBUG){ Log.d(TAG, "onCreate, emailCB=true, sender is " + email
                + ", password is " + password + ", recipients are " + recipients);
        }

        // set Apply Button listener
        Button mApplyBtn = (Button)theView.findViewById(R.id.email_info_apply);
        mApplyBtn.setOnClickListener(mApplyBtnListener);

        return theView;
    }

    // handle Apply button
    Button.OnClickListener mApplyBtnListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // verify the email fields

            boolean emailCB = mEmailChkBx.isChecked();
            String email = mEmailET.getText().toString();
            String password = mPwET.getText().toString();
            String recipients = mRecipientsET.getText().toString();

            // contain error msg when email Checkbox is checked
            StringBuffer errorEInfoMsg = new StringBuffer();
            // contain error msg for input errors
            StringBuffer errorInputMsg = new StringBuffer();

            boolean validSender     = false;
            boolean validPassword   = false;
            String validRecipients  = null;

            // if there is no sender email
            if ( TextUtils.isEmpty(email)) {
                errorEInfoMsg.append("No sender gmail, ");
            } else {
                validSender = senderCheck(email);
                // if sender email is not valid
                if (!validSender) {
                    mEmailET.setText("");
                    errorInputMsg.append('"' + email + '"' + " is invalid sender gmail, ");
                    email = "";
                }
            }

            // if there is no password
            if ( TextUtils.isEmpty(password)) {
                errorEInfoMsg.append("No sender password, ");
            } else {
                validPassword = passwordCheck(password);
                if (!validPassword) {
                    mPwET.setText("");
                    errorInputMsg.append("Invalid gmail password, ");
                }
            }

            // if there is no recipients
            // It is important to check for recipients null here,
            // so we only have one error msg in errorEInfoMsg, and no invalid message
            if ( TextUtils.isEmpty(recipients)) {
                errorEInfoMsg.append("No recipients, ");
            } else {
                validRecipients = null;
                String invalidRecipients = null;
                // recipients should be be null here
                ContentValues result = recipientsCheck(recipients);

                // result should not be null, for recipients is not null
                validRecipients = result.getAsString(EmailInfoFragment.VALID_RECIPIENTS_KEY);
                invalidRecipients = result.getAsString(EmailInfoFragment.INVALID_RECIPIENTS_KEY);


                if (TextUtils.isEmpty(validRecipients)) {
                    mRecipientsET.setText("");
                    errorEInfoMsg.append("No valid recipients, ");
                } else {
                    mRecipientsET.setText(validRecipients);
                }

                if (!TextUtils.isEmpty(invalidRecipients)) {
                    errorInputMsg.append("Invalid recipients are: " + invalidRecipients + ", ");
                }
            }

            // if the email checkbox is checked
            if ( emailCB ) {
                // check all Email info
                if ( !validSender || !validPassword || TextUtils.isEmpty(validRecipients)) {
                    emailCB = false;
                    mEmailChkBx.setChecked(false);
                }
                // display invalid input and empty fields error message
                String errorMsg = errorInputMsg.toString() + errorEInfoMsg.toString();
                if ( !TextUtils.isEmpty(errorMsg) ) {
                    // remove last ", " chars
                    errorMsg = errorMsg.substring(0, errorMsg.length() - 2);
                    // add " ." to the end and last info
                    errorMsg += ". Email Checkbox can not be checked.";

                    FragmentManager fm = getFragmentManager();
                    MyAlertDialogFragment alertDialog = MyAlertDialogFragment.newInstance("Email Info Errors", errorMsg);
                    alertDialog.show(fm, "email_info_alert");
                }
            } else {
                // display invalid input errors msg
                String errorMsg = errorInputMsg.toString();
                if ( !TextUtils.isEmpty(errorMsg)) {
                    // remove last ", " chars
                    errorMsg = errorMsg.substring( 0, errorMsg.length()-2);
                    // add " ." to the end
                    errorMsg += ".";

                    FragmentManager fm = getFragmentManager();
                    MyAlertDialogFragment alertDialog = MyAlertDialogFragment.newInstance("Email Input Errors", errorMsg);
                    alertDialog.show(fm, "email_input_alert");
                }
            }

            SharedPreferences.Editor prefsED = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, 0).edit();

            prefsED.putBoolean(SPKeys.EMAILCB_KEY, emailCB);
            prefsED.putString(SPKeys.EMAILET_KEY, email);
            prefsED.putString(SPKeys.PWET_KEY, password);
            prefsED.putString(SPKeys.RECIPIENTS_KEY, validRecipients);
            prefsED.commit();
        }
    };

    public static boolean senderCheck(String email) {
        // sender gmail must be longer than 11 chars, since "@gmail.com"=10 chrs
        if ( TextUtils.isEmpty(email) || (email.length() < 10) ) {
            return false;
        } else {
            String eDomain = email.substring(email.length() - 10);
            // check for gmail only
            if (!eDomain.equalsIgnoreCase("@gmail.com")) {
               return false;
            }
            return true;
        }
    }

    public static boolean passwordCheck(String password) {
        // 2011 Google change minimum password to 8 chars (old is 6 chars)
        if ( TextUtils.isEmpty(password) || (password.length() < 8) ) {
            return false;
        }
        return true;
    }

    /*
     use EmailValidator class to verify each email
     put valid emails into validRecipients, invalid emails into invalidRecipients
     separated them with ", "
     put them into ContentValues to return them
     */
    public static ContentValues recipientsCheck(String recipients) {

        StringBuffer validRecipients = new StringBuffer();
        StringBuffer invalidRecipients = new StringBuffer();
        // create result contentValues object
        ContentValues result = new ContentValues();

        EmailValidator emailValidator = new EmailValidator();

        // assume recipients should not be null
        String[] recipientsAry = recipients.split(",");

        for ( int i=0; i < recipientsAry.length; i++) {

            boolean validCheck = emailValidator.validate(recipientsAry[i]);
            if (validCheck) {
                // add it to validRecipients
                validRecipients.append(recipientsAry[i] + ", ");

            } else {
                // add it to invalidRecipients
                invalidRecipients.append(recipientsAry[i] + ", ");
            }
        }

        String vRecipients = validRecipients.toString();
        String invRecipients = invalidRecipients.toString();
        // remove the last ", " 2 chars
        if ( !TextUtils.isEmpty(vRecipients) ) {
            vRecipients = vRecipients.substring(0, vRecipients.length() - 2);
            result.put(VALID_RECIPIENTS_KEY, vRecipients);
        }
        if ( !TextUtils.isEmpty(invRecipients)) {
            invRecipients = invRecipients.substring(0, invRecipients.length() - 2);
            result.put(INVALID_RECIPIENTS_KEY, invRecipients);
        }

        return result;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
