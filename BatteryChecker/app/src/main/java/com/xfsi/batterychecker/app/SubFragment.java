package com.xfsi.batterychecker.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SubFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SubFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SubFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SubFragment";
    public static final boolean DEBUG = false;

   // private static final String PREFS_NAME = "com.xfsi.batterychecker";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // These Views are also used in onResume()
    private View theView;
    private TextView mLevelTx;
    private TextView mIntervalTx;
    private TextView mNotify1;
    private TextView mNotify2;
    private TextView mNotify3;



    private OnFragmentInteractionListener mListener;    // this is important

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SubFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubFragment newInstance(String param1, String param2) {
        SubFragment fragment = new SubFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SubFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // get info from level,intervals fragments here
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle bundle) {
        theView = inflater.inflate(R.layout.fragment_sub, container, false);

        // I need to set all TextViews here
        mLevelTx = (TextView) theView.findViewById(R.id.level);
        mIntervalTx = (TextView) theView.findViewById(R.id.interval);

        // get the three notify image Views
        mNotify1 = (TextView)theView.findViewById(R.id.notify1);
        mNotify2 = (TextView)theView.findViewById(R.id.notify2);
        mNotify3 = (TextView)theView.findViewById(R.id.notify3);



        showSubFragment();

        // when user click on Level icon
        View level_icon = theView.findViewById(R.id.level_icon);
        level_icon.setOnClickListener(this);

        // when user click on time interval icon
        View interval_icon = theView.findViewById(R.id.interval_icon);
        interval_icon.setOnClickListener(this);

        // when user click on time notify icon
        View notify_icon = theView.findViewById(R.id.notify_icon);
        notify_icon.setOnClickListener(this);

        if (DEBUG) { Log.d(TAG, "Finished create SubFragment");}
        return theView;
    }

    // Handle click listener for level, interval, notify icons
    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();

        // find the selected fragment, then replace it with mainContainer
        int selectedFgId = v.getId();
        Fragment selectedFragment = null;

        switch (selectedFgId) {
            case R.id.level_icon:
                selectedFragment = LevelFragment.newInstance(null, null);
                break;
            case R.id.interval_icon:
               selectedFragment = TimeIntervalFragment.newInstance(null, null);
                break;
            case R.id.notify_icon:
                selectedFragment = NotifyFragment.newInstance(null, null);
                break;
            default:
                break;
        }

        if (null != selectedFragment) {
            FragmentTransaction ft = fm.beginTransaction();
            //ft.replace(R.id.mainContainer, levelFragment, "SubFragment_Tag");
            ft.replace(R.id.mainContainer, selectedFragment, "SubFragment_Tag");
            ft.addToBackStack("SubFragment_Tag");
            ft.commit();
        }
    }

/*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Bundle bundle) {
        if (mListener != null) {
            mListener.onFragmentInteraction(bundle);
        }
    }*/

    // I need this resume, because from notify activity, back btn, subFragment didn't get update the
    // values change in notify activity
    @Override
    public void onResume(){
        // this work, the first time onResume() theView is null,
        // but after that theView is valid.
        super.onResume();
        if (null != theView ) {
            showSubFragment();
        }

    }

    // restore values from share prefs, then display SubFragment
    private void showSubFragment() {

        // only getActivity() could be problem, I may want to pass in context
        SharedPreferences prefs = getActivity().getSharedPreferences(SPKeys.PREFS_NAME, Context.MODE_PRIVATE);

        // retrieve level value in share Pref
        //when it is the first time, there no value in share prefs
        // so it go to exception
        int level;
        try {
            level = prefs.getInt(SPKeys.LEVEL_KEY, 40);
        }catch (Exception e) {
            level = 40;
        }
        mLevelTx.setText(String.valueOf(level));

        // retrieve interval value in share Pref
        int interval;
        try {
            interval = prefs.getInt(SPKeys.INTERVAL_KEY, 12);
        } catch (Exception e) {
            interval = 12;
        }
        mIntervalTx.setText(String.valueOf(interval));

        //retrieve notify methods in share pref
        boolean emailCB = prefs.getBoolean(SPKeys.EMAILCB_KEY, false);
        boolean textCB = prefs.getBoolean(SPKeys.TEXTCB_KEY, false);
        boolean notifyCB = prefs.getBoolean(SPKeys.NOTIFY_KEY, false);

        // I got all true, but I only get top two - email text, didn't see notify
        if (DEBUG) {
            Log.d(TAG, "showSubFragment(), email is " + emailCB + ", text is " + textCB
                    + ", notify " + notifyCB);
        }

        int count = 0;
        if (emailCB) {
            mNotify1.setText("Email");
            count++;
        }
        if (textCB) {
            if (count == 0){
                mNotify1.setText("Text");
            } else {
                mNotify2.setText("Text");
            }
            count++;
        }
        if (notifyCB) {
            if (count == 0){
                mNotify1.setText("Notify");
            } else if (count == 1) {
                mNotify2.setText("Notify");
            } else {
                mNotify3.setText("Notify");
            }
            count++;
        }

        // now I need to hide the empty notify textviews
        switch (count){
            case 0:
                // hide all notify textviews
                mNotify1.setVisibility(View.INVISIBLE);
                mNotify2.setVisibility(View.INVISIBLE);
                mNotify3.setVisibility(View.INVISIBLE);
                break;
            case 1:
                // hide notify2 and notify3, but I need to turn notify1 back to visible
                mNotify1.setVisibility(View.VISIBLE);
                mNotify2.setVisibility(View.INVISIBLE);
                mNotify3.setVisibility(View.INVISIBLE);
                break;
            case 2:
                // hide notify3, notify1 and notify2 back to visible
                mNotify3.setVisibility(View.INVISIBLE);
                mNotify1.setVisibility(View.VISIBLE);
                mNotify2.setVisibility(View.VISIBLE);
                break;
            case 3:
                // All should be visible
                mNotify1.setVisibility(View.VISIBLE);
                mNotify2.setVisibility(View.VISIBLE);
                mNotify3.setVisibility(View.VISIBLE);
                break;
            default:
                break;
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
        //public void onFragmentInteraction(Bundle bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

}
