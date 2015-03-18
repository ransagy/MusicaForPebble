package com.ransagy.musicaforpebble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set this class to listen to click events on our buttons.
        findViewById(R.id.sendIntentButton).setOnClickListener(this);

        // Only if we're not running yet, start our service with an explicit intent.
        if (!MetaChangedService.IsRunning) {
            Intent i = new Intent("com.ransagy.musicaforpebble.MetaChangedService");
            i.setClass(this, MetaChangedService.class);
            this.startService(i);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendIntentButton:
                // Make an intent to mimic a real metadata change broadcast.
                Intent i = new Intent(MetaChangedService.MUSIC_METADATACHANGED);

                // Use the UI values for the intent.
                i.putExtra(MetaChangedService.ARTIST, ((TextView) findViewById(R.id.txtArtist)).getText().toString());
                i.putExtra(MetaChangedService.TRACK, ((TextView) findViewById(R.id.txtTrack)).getText().toString());
                i.putExtra(MetaChangedService.ALBUM, ((TextView) findViewById(R.id.txtAlbum)).getText().toString());

                sendBroadcast(i);
                break;
            case R.id.sendAlternateIntentButton:
                // Make an intent to mimic a metadata change broadcast that the pebble app listens to.
                Intent ap = new Intent(MetaChangedService.PEBBLE_NOW_PLAYING);

                // Use the UI values for the intent.
                float maxArtist = GeneralUtils.TryParseFloat(((TextView) findViewById(R.id.maxArtistLine)).getText().toString(),MetaChangedService.MAX_WIDTH_PER_ARTIST_LINE);
                float maxTrack = GeneralUtils.TryParseFloat(((TextView) findViewById(R.id.maxTrackLine)).getText().toString(),MetaChangedService.MAX_WIDTH_PER_TRACK_LINE);
                ap.putExtra(MetaChangedService.ARTIST, RTLHelper.ReorderTextForRTLAlt(((TextView) findViewById(R.id.txtArtist)).getText().toString(), maxArtist));
                ap.putExtra(MetaChangedService.TRACK, RTLHelper.ReorderTextForRTLAlt(((TextView) findViewById(R.id.txtTrack)).getText().toString(), maxTrack));
                ap.putExtra(MetaChangedService.ALBUM, RTLHelper.ReorderTextForRTLAlt(((TextView) findViewById(R.id.txtAlbum)).getText().toString(), 0));

                sendBroadcast(ap);
                break;
            case R.id.defaultToAltCheckBox:
                RTLHelper.CurrentMode = ((CheckBox)findViewById(R.id.defaultToAltCheckBox)).isChecked() ? 1 : 0;
                break;
            case R.id.debugButton:
                GeneralUtils.DebugSomething();
                break;
        }
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
