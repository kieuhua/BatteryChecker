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
 * {@link LevelFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LevelFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LevelFragment extends Fragment {

    private static final String TAG = "LevelFragment";
    public static final boolean DEBUG = false;

    //private static final String PREFS_NAME = "com.xfsi.batterychecker";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    EditText mLevelTx;      // EditText of level
    RadioButton mSelectedRB;

    // this is the view for TimeIntervalFragment, it is created in onCreateView(),
    // and it is also used in onResume()
    private View theView;
    private RadioGroup mRadioG;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LevelFragment newInstance(String param1, String param2) {
        LevelFragment fragment = new LevelFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LevelFragment() {
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
        theView = inflater.inflate(R.layout.fragment_level, container, false);
        mRadioG = (RadioGroup) theView.findViewById(R.id.radioGLevel);

        // get level value from share prefs,
        SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE );
        int level = prefs.getInt(SPKeys.LEVEL_KEY, 40);
        mLevelTx = (EditText)theView.findViewById(R.id.level_text);

        // switch on on level, then check radio button, and setText
        switch (level) {
            case 20:
                mRadioG.check(R.id.radio_l20);
                mLevelTx.setText("20");
                break;
            case 40:
                mRadioG.check(R.id.radio_l40);
                mLevelTx.setText("40");
                break;
            case 60:
                mRadioG.check(R.id.radio_l60);
                mLevelTx.setText("60");
                break;
            default:
                if (level > 0) {
                    mRadioG.check(R.id.radio_custom);
                    mLevelTx.setText(String.valueOf(level));
                }
                break;
        }

        // Handle Apply button click
        Button applyBtn = (Button)theView.findViewById(R.id.level_apply);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            // the view v here is apply btn, so I can not use it to find Level EditText, or any other view
            // for it is compiled, but it gives me null pointer
            public void onClick(View v) {
                SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, 0);
                SharedPreferences.Editor prefsEd = getActivity().getSharedPreferences(SPKeys.PREFS_NAME,0).edit();
                // if user do nothing, then hit Apply Btn, then I still need
                // to initialize level with previous value
                // I am not I need it, but it doesn't hurt
                int level = prefs.getInt(SPKeys.LEVEL_KEY, 40);
                // need to get selectedRBId from RadioGroup
                int selectedRBId = mRadioG.getCheckedRadioButtonId();

                // get the value of selected Level Radio button, and save to share prefs
                switch (selectedRBId) {
                    case R.id.radio_l20:
                        mLevelTx.setText("20");
                        level = 20;
                        break;
                    case R.id.radio_l40:
                        mLevelTx.setText("40");
                        level = 40;
                        break;
                    case R.id.radio_l60:
                        mLevelTx.setText("60");
                        level = 60;
                        break;
                    case R.id.radio_custom:
                        // get user input of the value of custom level
                        String levelStr = mLevelTx.getText().toString().trim();
                        // validate the input
                        level = Integer.parseInt(levelStr);
                        level %= 100;
                        if (level == 0 ){
                            level = 100;
                        }
                        mLevelTx.setText(String.valueOf(level));
                        break;
                    default:
                        break;
                }
                // save level in share prefs
                prefsEd.putInt(SPKeys.LEVEL_KEY, level);
                prefsEd.commit();
                if (DEBUG){ Log.d(TAG, "ApplyBtn click, current level is: " + level); }
            }
        });
        return theView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Bundle bundle) {
        if (mListener != null) {
            mListener.onFragmentInteraction(bundle);
        }
    }*/

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
       // public void onFragmentInteraction(Bundle bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
