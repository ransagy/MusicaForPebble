package com.ransagy.musicaforpebble;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.sendIntentButton).setOnClickListener(this);

        if (!MetaChangedService.IsRunning)
        {
            Intent i = new Intent("com.ransagy.musicaforpebble.MetaChangedService");
            i.setClass(this, MetaChangedService.class);
            this.startService(i);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.sendIntentButton:
                Intent i = new Intent(MetaChangedService.MUSIC_METADATACHANGED);

                String artist = ((TextView)findViewById(R.id.txtArtist)).getText().toString();
                String track = ((TextView)findViewById(R.id.txtTrack)).getText().toString();
                String album = ((TextView)findViewById(R.id.txtAlbum)).getText().toString();

                i.putExtra(MetaChangedService.ARTIST,artist);
                i.putExtra(MetaChangedService.TRACK,track);
                i.putExtra(MetaChangedService.ALBUM,album);

                sendBroadcast(i);
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
