package com.xfsi.batterychecker.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;


public class CheckLevelActivity extends Activity implements LevelFragment.OnFragmentInteractionListener{
    private RadioGroup radioGLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_level);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.check_level, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       // int id = item.getItemId();
      //  if (id == R.id.action_settings) {
       //     return true;
        //}
       // return super.onOptionsItemSelected(item);
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
