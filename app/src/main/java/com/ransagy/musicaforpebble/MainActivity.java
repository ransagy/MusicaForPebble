package com.ransagy.musicaforpebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    private String[] metaArray = null;

    private BroadcastReceiver mServiceDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract metadata from intent.
            metaArray = intent.getStringArrayExtra(MetaChangedService.LOCAL_DATA);

            // Handle Android UI operations in a handler
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    // Set UI fields to the new values.
                    ((TextView) findViewById(R.id.txtArtistMain)).setText(metaArray[0]);
                    ((TextView) findViewById(R.id.txtTrackMain)).setText(metaArray[1]);
                    ((TextView) findViewById(R.id.txtAlbumMain)).setText(metaArray[2]);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Only if we're not running yet, start our service with an explicit intent.
        if (!MetaChangedService.IsRunning) {
            Intent i = new Intent(MetaChangedService.class.getName());
            i.setClass(this, MetaChangedService.class);
            this.startService(i);
        }

        // Register for changes sent from our service.
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mServiceDataReceiver, new IntentFilter(MetaChangedService.LOCAL_INTENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
