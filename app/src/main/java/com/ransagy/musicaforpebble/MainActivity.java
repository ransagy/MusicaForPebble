package com.ransagy.musicaforpebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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
}
