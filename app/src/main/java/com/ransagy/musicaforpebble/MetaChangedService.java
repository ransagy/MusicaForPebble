package com.ransagy.musicaforpebble;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaChangedService extends Service {

    public static final String MUSIC_METADATACHANGED = "com.android.music.metachanged";
    public static final String TRACK = "track";
    public static final String ALBUM = "album";
    public static final String ARTIST = "artist";
    private static final String PEBBLE_NOW_PLAYING = "com.getpebble.action.NOW_PLAYING";
    public static boolean IsRunning = false;
    private final String LOG_TAG = this.getClass().getSimpleName();

    private BroadcastReceiver mMetaChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract metadata from intent.
            String artist = intent.getStringExtra(ARTIST);
            String album = intent.getStringExtra(ALBUM);
            String track = intent.getStringExtra(TRACK);
            String newArtist = null, newAlbum = null, newTrack = null;

            // Compile and attempt to match Hebrew characters in the metadata.
            Pattern p = Pattern.compile("\\p{InHebrew}", Pattern.UNICODE_CASE);
            Matcher mAr = p.matcher(artist);
            Matcher mAl = p.matcher(album);
            Matcher mT = p.matcher(track);

            boolean didModify = false;

            // For any part that contains Hebrew, Modify the text and flag we made a change.
            if (mAr.find()) {
                newArtist = RTLHelper.ReorderTextForRTL(artist, true);
                didModify = true;
            }

            if (mAl.find()) {
                newAlbum = RTLHelper.ReorderTextForRTL(album, false);
                didModify = true;
            }

            if (mT.find()) {
                newTrack = RTLHelper.ReorderTextForRTL(track, true);
                didModify = true;
            }

            // Only if anything was changed, we'll send a new intent.
            if (didModify) {

                Log.v(LOG_TAG, "Before:");
                Log.v(LOG_TAG, artist + ":" + album + ":" + track);

                Log.v(LOG_TAG, "After:");
                Log.v(LOG_TAG, newArtist + ":" + newAlbum + ":" + newTrack);

                final Intent i = new Intent(PEBBLE_NOW_PLAYING);
                i.putExtra(ARTIST, newArtist);
                i.putExtra(ALBUM, newAlbum);
                i.putExtra(TRACK, newTrack);

                sendBroadcast(i);
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public MetaChangedService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Register our metadata change receiver and flag we're running already.
        registerReceiver(mMetaChangedReceiver, new IntentFilter(MUSIC_METADATACHANGED));
        IsRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cleanup receiver and flag.
        unregisterReceiver(mMetaChangedReceiver);
        IsRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We're not binding in this service.
        return null;
    }
}
