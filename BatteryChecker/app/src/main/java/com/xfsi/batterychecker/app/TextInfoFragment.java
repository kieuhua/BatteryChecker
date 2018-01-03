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
 * {@link TextInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TextInfoFragment extends Fragment {
    private static final String TAG = "TextInfoFragment";
    public static final boolean DEBUG = false;

    protected static final String VALID_PHONES_KEY = "valid_phones_key";
    protected static final String INVALID_PHONES_KEY = "invalid_phones_key";
    protected static final String FIVE_PLUS_KEY = "five_plus_key";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private View theView;
    private CheckBox mTextChkBx;
    private EditText mPhoneNumsET;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TextInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TextInfoFragment newInstance(String param1, String param2) {
        TextInfoFragment fragment = new TextInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public TextInfoFragment() {
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
        theView = inflater.inflate(R.layout.fragment_text_info, container, false);

        mTextChkBx = (CheckBox)theView.findViewById(R.id.textChkBx);
        mPhoneNumsET = (EditText)theView.findViewById(R.id.phoneNums);

        SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, 0);
        // retrieve text info from share prefs
        boolean textCB = prefs.getBoolean(SPKeys.TEXTCB_KEY, false);
        String phoneNums = prefs.getString(SPKeys.PHONENUMS_KEY, null);

        // make sure the Text checkbox enable at all time in this fragment
        mTextChkBx.setEnabled(true);

        // set checkbox and text with previous values
        if (textCB) {
            mTextChkBx.setChecked(true);
        }
        mPhoneNumsET.setText(phoneNums);

        if (DEBUG){ Log.i(TAG, "onCreateView, phoneNums are:" + phoneNums+ ".");}

        // set Apply Button listener
        Button mApplyBtn = (Button)theView.findViewById(R.id.text_info_apply);
        mApplyBtn.setOnClickListener(mApplyBtnListener);

        return theView;
    }

    // Handler Apply button click
    Button.OnClickListener mApplyBtnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            // contain error msg when text Checkbox is checked
            StringBuffer errorTInfoMsg = new StringBuffer();
            // contain error msg for input errors
            StringBuffer errorInputMsg = new StringBuffer();

            boolean fivePlus = false;

            String validPhones  = null;  // I need it here, so I can use it

            boolean textCB = mTextChkBx.isChecked();
            String phoneNums = mPhoneNumsET.getText().toString();

            // if there is no phone
            // It is important to check for recipients null here,
            // so we only have one error msg in errorEInfoMsg, and no invalid message
            if ( TextUtils.isEmpty(phoneNums)) {
                errorTInfoMsg.append("No phone number, ");
            } else {
                // assume phoneNums is not null
                ContentValues result = validatePhoneNums(phoneNums);

                // result should not be null, for phoneNums is not null
                // but validPhones, invalidPhones  can be null
                validPhones = result.getAsString(VALID_PHONES_KEY);
                String invalidPhones = result.getAsString(INVALID_PHONES_KEY);

                // result.getAsBoolean(..) may return null
                // if the value is missing or cannot be converted
                // need to check for null; otherwise you may get null Exception
                if ( null != result.getAsBoolean(FIVE_PLUS_KEY)) {
                    fivePlus = result.getAsBoolean(FIVE_PLUS_KEY);
                }

                if (TextUtils.isEmpty(validPhones)) {
                    mPhoneNumsET.setText("");
                    errorTInfoMsg.append("No valid phone numbers, ");
                } else {
                    mPhoneNumsET.setText(validPhones);
                }

                if (!TextUtils.isEmpty(invalidPhones)) {
                    errorInputMsg.append("Invalid phone numbers are: " + invalidPhones + ", ");
                }
                if (fivePlus) {
                    errorInputMsg.append("More than 5 phone numbers, ");
                }
            }

            // if the email checkbox is checked
            if ( textCB ) {
                // check valid phones for null
                if (null == validPhones) {
                    textCB = false;
                    mTextChkBx.setChecked(false);
                }
                // display invalid input and empty fields error message
                String errorMsg = errorInputMsg.toString() + errorTInfoMsg.toString();
                if ( !TextUtils.isEmpty(errorMsg) ) {
                    // remove last ", " chars
                    errorMsg = errorMsg.substring( 0, errorMsg.length()-2);
                    // add " ." to the end
                    errorMsg += ". Text Checkbox can not be checked.";

                    FragmentManager fm = getFragmentManager();
                    MyAlertDialogFragment alertDialog = MyAlertDialogFragment.newInstance("Text Info Errors", errorMsg);
                    alertDialog.show(fm, "text_info_alert");
                }
            }  else {
                // display invalid input errors msg
                String errorMsg = errorInputMsg.toString();
                if ( !TextUtils.isEmpty(errorMsg) ) {
                    // remove last ", " chars
                    errorMsg = errorMsg.substring( 0, errorMsg.length()-2);
                    // add " ." to the end
                    errorMsg += ".";

                    FragmentManager fm = getFragmentManager();
                    MyAlertDialogFragment alertDialog = MyAlertDialogFragment.newInstance("Text Input Errors", errorMsg);
                    alertDialog.show(fm, "text_input_alert");

               }
            }

            SharedPreferences.Editor prefsED = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, 0).edit();

            prefsED.putBoolean(SPKeys.TEXTCB_KEY, textCB);
            prefsED.putString(SPKeys.PHONENUMS_KEY, validPhones);
            prefsED.commit();
        }
    };

    // validate phoneNumsStr, then return String validPhones
    // assume phoneNumsStr is not null
    public static ContentValues validatePhoneNums(String phoneNumsStr) {

        StringBuffer validPhones = new StringBuffer();
        StringBuffer invalidPhones = new StringBuffer();

        ContentValues result = new ContentValues();

        //if something in phoneNumsStr, then verify them
        int ctn = 0;  // count for valid phone numbers
        String[] phoneNums = phoneNumsStr.split(",");

        if (phoneNums.length > 5) {
            //  5 numbers only
            result.put(FIVE_PLUS_KEY, phoneNums.length);
        }
        int len = phoneNums.length;
        for (int i = 0; (i < phoneNums.length && i < 5); i++) {
            String phone = phoneNums[i];
            // I need to return null or valid phone string
            String validPhone = checkPhone(phone);

            if (null == validPhone) {
                invalidPhones.append(phone + ", ");
                continue; // skip this number
            } else {
                // format phone to (888) 999-0000
                String validPStr = formatPhone(validPhone);
                validPhones.append(validPStr + ", ");
            }
        }

        String validPhoneStr = validPhones.toString();
        String invalidPhoneStr = invalidPhones.toString();
        // remove the last 2 chars
        if (!TextUtils.isEmpty(validPhoneStr)) {
            validPhoneStr = validPhoneStr.substring(0, validPhoneStr.length() - 2);
            result.put(VALID_PHONES_KEY, validPhoneStr);
        }
        if (!TextUtils.isEmpty(invalidPhoneStr)) {
            invalidPhoneStr = invalidPhoneStr.substring(0, invalidPhoneStr.length() - 2);
            result.put(INVALID_PHONES_KEY, invalidPhoneStr);
        }

        return result;
    }

    //create phone number format, (978) 888-1234
    public static String formatPhone(String phone) {
        String phoneStr = phone;

        //use String manipulation to create the string = "(978) 888-1234"
        String first = phoneStr.substring(0,3);
        String middle = phoneStr.substring(3,6);
        String last = phoneStr.substring(6);

        String phoneFormatted = String.format("(%s) %s-%s", first, middle, last);
        return phoneFormatted;
    }

    /* take out "(", ")", "-", spaces,
        check for only digits => return null
     */
    public static String checkPhone(String phone){
        String phoneStr = phone;

        // take out "(" and ")"
        phoneStr = phoneStr.replace("(", "");
        phoneStr = phoneStr.replace(")", "");
        // take out the "-"
        phoneStr = phoneStr.replace("-", "");
        // removes all whitespaces and non visible characters such \tab, \n
        phoneStr = phoneStr.replaceAll("\\s+", "");

        if ( !TextUtils.isDigitsOnly(phoneStr) || (phoneStr.length() != 10) ) {
            // it is not all digits
            // why the first invalid number is crashed???
            return null;
        } else {
            return phoneStr;
        }
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
