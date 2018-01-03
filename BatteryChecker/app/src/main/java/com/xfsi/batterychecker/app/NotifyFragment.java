package com.xfsi.batterychecker.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotifyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotifyFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class NotifyFragment extends Fragment {

    private static final String TAG = "NotifyFragment";
    public static final boolean DEBUG = false;

   //
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private View theView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotifyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotifyFragment newInstance(String param1, String param2) {
        NotifyFragment fragment = new NotifyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public NotifyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if I set this, I may not need onResume() then
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
        theView =  inflater.inflate(R.layout.fragment_notify, container, false);

        showNotifyFragment(theView);

        // Handle Apply button click
        // not until the user click "Apply" button, before I save the checkboxes setting to Share prefs
        Button applyBtn = (Button)theView.findViewById(R.id.notify_apply);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            boolean email, text, notify;    // for Debug use

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefsEd = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE ).edit();

                // if ( emailChkBx, textChkBx, notifyChkBx is checked) => save it to share prefs
                CheckBox emailChkBx = (CheckBox)theView.findViewById(R.id.emailChkBx);
                if (emailChkBx.isChecked()) {
                    prefsEd.putBoolean(SPKeys.EMAILCB_KEY, true);
                   email = true;
                } else {
                    prefsEd.putBoolean(SPKeys.EMAILCB_KEY, false);
                }
                CheckBox textChkBx = (CheckBox)theView.findViewById(R.id.textChkBx);
                if (textChkBx.isChecked()) {
                    prefsEd.putBoolean(SPKeys.TEXTCB_KEY, true);
                    text = true;
                } else {
                    prefsEd.putBoolean(SPKeys.TEXTCB_KEY, false);
                }
                CheckBox notifyChkBx = (CheckBox)theView.findViewById(R.id.notifyChkBx);
                if ( notifyChkBx.isChecked()) {
                    prefsEd.putBoolean(SPKeys.NOTIFY_KEY, true);
                   notify = true;
                } else {
                    prefsEd.putBoolean(SPKeys.NOTIFY_KEY, false);
                }
                prefsEd.commit(); // this commit() didn't update share prefs ???

                if (DEBUG){
                    Log.d(TAG, "onCreateView: Apply btn clicked, email is " + email + ", text is "
                             + text + ", notify is "+ notify);
                }
            }
        });
        return theView;
    }

   // setChecked for emailCB, textCB, notifyCB
    private void showNotifyFragment(View view) {

        CheckBox emailChkBx = (CheckBox)view.findViewById(R.id.emailChkBx);
        CheckBox textChkBx = (CheckBox)view.findViewById(R.id.textChkBx);
        CheckBox notifyChkBx = (CheckBox)view.findViewById(R.id.notifyChkBx);

        // get the email and text textview messaage
        TextView eMsgTx = (TextView)theView.findViewById(R.id.eMsgTx);
        TextView tMsgTx = (TextView)theView.findViewById(R.id.tMsgTx);

        // need to read in check boxes from share prefs to restore the previous setting
        SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE );
        boolean emailCB = prefs.getBoolean(SPKeys.EMAILCB_KEY, false);
        boolean textCB = prefs.getBoolean(SPKeys.TEXTCB_KEY, false);
        boolean notifyCB = prefs.getBoolean(SPKeys.NOTIFY_KEY, false);

        if (DEBUG){
            Log.i(TAG, "onCreateView: after retrieve from share prefs: " + "email is " + emailCB
                    + ", text is " + textCB + ", notify is " + notifyCB);
        }
        String email = prefs.getString(SPKeys.EMAILET_KEY, null);
        String password = prefs.getString(SPKeys.PWET_KEY, null);
        String recipients = prefs.getString(SPKeys.RECIPIENTS_KEY, null);

        if ((TextUtils.isEmpty(email)) || (TextUtils.isEmpty(password)) || (TextUtils.isEmpty(recipients))) {
            // if one of these fields(sender, password, recipients) is empty, then disable and uncheck emailChkBC
            emailChkBx.setChecked(false);
            emailChkBx.setEnabled(false);
            eMsgTx.setVisibility(View.VISIBLE);
        } else {
            // if all fields(sender, password, recipients are filled, then enable emailChkBC
            emailChkBx.setEnabled(true);
            eMsgTx.setVisibility(View.INVISIBLE);
            // and if the emailCB is checked from share prefs
            if (emailCB) {
                emailChkBx.setChecked(true);
            }
        }

        String phoneNums = prefs.getString(SPKeys.PHONENUMS_KEY, null);

        // if phoneNums is empty, then disable and uncheck textChkBC
        if (TextUtils.isEmpty(phoneNums)) {
            textChkBx.setChecked(false);
            textChkBx.setEnabled(false);
            tMsgTx.setVisibility(View.VISIBLE);
        } else {
            textChkBx.setEnabled(true);
            tMsgTx.setVisibility(View.INVISIBLE);
            // and if the textCB is checked from share prefs
            if (textCB) {
                textChkBx.setChecked(true);
            }
        }

        if (notifyCB) {
            notifyChkBx.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        // do I need this since I have setRetainInstance(true); ???
        // need to think more about it
        // setChecked for emailCB, textCB, notifyCB
        showNotifyFragment(theView);
        super.onResume();
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
