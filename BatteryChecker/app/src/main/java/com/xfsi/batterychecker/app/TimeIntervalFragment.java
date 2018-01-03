package com.xfsi.batterychecker.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimeIntervalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimeIntervalFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TimeIntervalFragment extends Fragment {

    private static final String TAG = "TimeIntervalFragment";
    public static final boolean DEBUG = false;

   // private static final String PREFS_NAME = "com.xfsi.batterychecker";

    public static final String ACTION_EXPLICIT_UPDATE = "com.xfsi.batterychecker.action.EXPLICIT_UPDATE";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    RadioButton mSelectedRB;
    EditText mIntervalTx;      // EditText of Interval

    // this is the view for TimeIntervalFragment, it is created in onCreateView(),
    // and it is also used in onResume()
    private View theView;
    RadioGroup mRadioG;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimeIntervalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimeIntervalFragment newInstance(String param1, String param2) {
        TimeIntervalFragment fragment = new TimeIntervalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public TimeIntervalFragment() {
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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        theView = inflater.inflate(R.layout.fragment_time_interval, container, false);
        mRadioG = (RadioGroup)theView.findViewById(R.id.radioGInterval);

        // retrieve the interval radio button id from Share pref
        SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE );
        int interval = prefs.getInt(SPKeys.INTERVAL_KEY, 12);
        mIntervalTx = (EditText)theView.findViewById(R.id.interval_text);

        // switch on interval value to check radio button and set EditText
        switch (interval) {
            case 4:
                mRadioG.check(R.id.radio_h4);
                mIntervalTx.setText("4");
                break;
            case 8:
                mRadioG.check(R.id.radio_h8);
                mIntervalTx.setText("8");
                break;
            case 12:
                mRadioG.check(R.id.radio_h12);
                mIntervalTx.setText("12");
                break;
            default:
                if (interval > 0 ) {
                    interval %= 12;
                } else {
                    interval = 12;
                }
                mRadioG.check(R.id.radio_custom);
                mIntervalTx.setText(String.valueOf(interval));
                break;
        }

        // Handle Apply button click
        Button applyBtn = (Button) theView.findViewById(R.id.interval_apply);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            // the view v here is apply btn, so I can not use it to find Level EditText, or any other view
            // for it is compiled, but it gives me null pointer
            public void onClick(View v) {

                SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, 0);
                int interval = prefs.getInt(SPKeys.INTERVAL_KEY, 12);

                SharedPreferences.Editor prefsEd = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE ).edit();

                // switch on selected radio button to set edittext
                int selectedRBId = mRadioG.getCheckedRadioButtonId();
                switch (selectedRBId) {
                    case R.id.radio_h4:
                        mIntervalTx.setText("4");
                        interval = 4;
                        break;
                    case R.id.radio_h8:
                        mIntervalTx.setText("8");
                        interval = 8;
                        break;
                    case R.id.radio_h12:
                        mIntervalTx.setText("12");
                        interval = 12;
                        break;
                    case R.id.radio_custom:
                        // get the user input value of custom interval
                        String levelStr = mIntervalTx.getText().toString().trim();
                        // validate the input
                        interval = Integer.parseInt(levelStr);
                        interval %= 12;
                        if ( 0 == interval) {
                            interval = 12;
                        }
                        mIntervalTx.setText(String.valueOf(interval));
                        break;
                    default:
                        interval = 12;
                       // prefsEd.putInt(BatteryAppWidgetConfigureActivity.INTERVAL_KEY, 4);
                        break;
                }
                // save interval in share prefs
                prefsEd.putInt(SPKeys.INTERVAL_KEY, interval);
                prefsEd.commit();

                // call to set the alarm, interval is hours
                // if interval is 12 hours, then we don't need to set the Alarm,
                // because, I also set the widget update in widget configuration for 12 hours
                if (interval != 12) {
                    AlarmUtil.setAlarmAtInterval(interval, getActivity());
                }
                if (DEBUG){ Log.d(TAG, "ApplyBtn click, current Interval is: " + interval); }
            }
        });

        return theView;
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
