package com.ransagy.musicaforpebble;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaChangedService extends Service {

    public static final String MUSIC_METADATACHANGED = "com.android.music.metachanged";
    public static final String TRACK = "track";
    public static final String ALBUM = "album";
    public static final String ARTIST = "artist";
    public static final String PEBBLE_NOW_PLAYING = "com.getpebble.action.NOW_PLAYING";
    public static boolean IsRunning = false;
    public static int MAX_CHARS_PER_TRACK_LINE = 10;
    public static int MAX_CHARS_PER_ARTIST_LINE = 15;

    public static float MAX_WIDTH_PER_TRACK_LINE = 63.0F;
    public static float MAX_WIDTH_PER_ARTIST_LINE = 65.0F;

    private final String LOG_TAG = this.getClass().getSimpleName();
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("71bfc6b0-3969-463d-857b-14ad6004224b");

    Handler uiHandler = new Handler();

    private BroadcastReceiver mMetaChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract metadata from intent.
            String artist = intent.getStringExtra(ARTIST);
            String album = intent.getStringExtra(ALBUM);
            String track = intent.getStringExtra(TRACK);

            boolean connected = PebbleKit.isWatchConnected(getApplicationContext());

            if (connected) {
                PebbleDictionary data = new PebbleDictionary();

                // Add a key of 1, and a string value.
                data.addString(0, artist);
                data.addString(1, track);
                data.addString(2, album);

                PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
            }

            /* Old fix-in-android code below */

            //            String newArtist = artist, newAlbum = album, newTrack = track;
            //
            //            if (artist != null && album != null && track != null) {
            //                // Compile and attempt to match Hebrew characters in the metadata.
            //                Pattern p = Pattern.compile("\\p{InHebrew}", Pattern.UNICODE_CASE);
            //                Matcher mAr = p.matcher(artist);
            //                Matcher mAl = p.matcher(album);
            //                Matcher mT = p.matcher(track);
            //
            //                boolean didModify = false;
            //
            //                // For any part that contains Hebrew, Modify the text and flag we made a change.
            //                if (mAr.find()) {
            //                    if (RTLHelper.CurrentMode == 0) {
            //                        newArtist = RTLHelper.ReorderTextForRTLDefault(artist, MAX_CHARS_PER_ARTIST_LINE);
            //                    } else {
            //                        newArtist = RTLHelper.ReorderTextForRTLAlt(artist, MAX_WIDTH_PER_ARTIST_LINE);
            //                    }
            //                    didModify = true;
            //                }
            //
            //                if (mAl.find()) {
            //                    if (RTLHelper.CurrentMode == 0) {
            //                        newAlbum = RTLHelper.ReorderTextForRTLDefault(album, 0);
            //                    } else {
            //                        newAlbum = RTLHelper.ReorderTextForRTLAlt(album, 0);
            //                    }
            //                    didModify = true;
            //                }
            //
            //                if (mT.find()) {
            //                    if (RTLHelper.CurrentMode == 0) {
            //                        newTrack = RTLHelper.ReorderTextForRTL(track, MAX_CHARS_PER_TRACK_LINE);
            //                    } else {
            //                        newTrack = RTLHelper.ReorderTextForRTLAlt(track, MAX_WIDTH_PER_TRACK_LINE);
            //                    }
            //                    didModify = true;
            //                }
            //
            //                // Only if anything was changed, we'll send a new intent.
            //                if (didModify) {
            //
            //                    Log.v(LOG_TAG, "Before:");
            //                    Log.v(LOG_TAG, artist + ":" + album + ":" + track);
            //
            //                    Log.v(LOG_TAG, "After:");
            //                    Log.v(LOG_TAG, newArtist + ":" + newAlbum + ":" + newTrack);
            //
            //                    final Intent i = new Intent(PEBBLE_NOW_PLAYING);
            //                    i.putExtra(ARTIST, newArtist);
            //                    i.putExtra(ALBUM, newAlbum);
            //                    i.putExtra(TRACK, newTrack);
            //
            //                    sendBroadcast(i);
            //                }
            //            }
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

        PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(LOG_TAG, "Pebble connected!");
            }

        });

        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(LOG_TAG, "Pebble disconnected!");
            }

        });

        PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveAck(Context context, int transactionId) {
                Log.i(LOG_TAG, "Received ack for transaction " + transactionId);
            }

        });

        PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveNack(Context context, int transactionId) {
                Log.i(LOG_TAG, "Received nack for transaction " + transactionId);
            }

        });

        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                Log.i(LOG_TAG, "Received value=" + data.getString(0) + " for key: 0");

                // Handle Android UI operations in a handler

                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        /* Update your UI here. */
                    }

                });

                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
            }

        });

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
