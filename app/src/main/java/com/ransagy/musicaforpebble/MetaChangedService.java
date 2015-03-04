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
            String artist = intent.getStringExtra(ARTIST);
            String album = intent.getStringExtra(ALBUM);
            String track = intent.getStringExtra(TRACK);

            Log.v(LOG_TAG, "Before:");
            Log.v(LOG_TAG, artist + ":" + album + ":" + track);

            Pattern p = Pattern.compile("\\p{InHebrew}", Pattern.UNICODE_CASE);
            Matcher mAr = p.matcher(artist);
            Matcher mAl = p.matcher(album);
            Matcher mT = p.matcher(track);

            if (mAr.find()) {
                artist = new StringBuilder(artist).reverse().toString();
            }

            if (mAl.find()) {
                album = new StringBuilder(album).reverse().toString();
            }

            if (mT.find()) {
                track = new StringBuilder(track).reverse().toString();
            }

            Log.v(LOG_TAG, "After:");
            Log.v(LOG_TAG, artist + ":" + album + ":" + track);

            final Intent i = new Intent(PEBBLE_NOW_PLAYING);
            i.putExtra(ARTIST, artist);
            i.putExtra(ALBUM, album);
            i.putExtra(TRACK, track);

            sendBroadcast(i);
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

        registerReceiver(mMetaChangedReceiver, new IntentFilter(MUSIC_METADATACHANGED));
        IsRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mMetaChangedReceiver);
        IsRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
