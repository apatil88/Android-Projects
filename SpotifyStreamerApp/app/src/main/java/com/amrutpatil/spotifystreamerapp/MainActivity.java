package com.amrutpatil.spotifystreamerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Activity uses the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            //If we are restored from a previous state, do nothing and return
            if (savedInstanceState != null) {
                return;
            }
            // New Fragment to be placed in this activity
            MainActivityFragment firstFragment = new MainActivityFragment();

            //Pass the Intent's extras to the fragment_container as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment_container to the FrameLayout 'fragment_container'
            getFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
