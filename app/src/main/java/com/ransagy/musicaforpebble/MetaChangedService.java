package com.ransagy.musicaforpebble;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MetaChangedService extends Service {

    public static boolean IsRunning = false;
    public static final String LOCAL_INTENT = "MetaChangedServiceUpdate";
    public static final String LOCAL_DATA = "Data";

    private AudioManager audioManagerInstance;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final Handler handler = new Handler();

    private Pair<String, String> lastArtist = new Pair<>("", "");
    private Pair<String, String> lastTrack = new Pair<>("", "");
    private Pair<String, String> lastAlbum = new Pair<>("", "");

    private BroadcastReceiver mMetaChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract metadata from intent.
            String artist = intent.getStringExtra(MetadataHelper.MetadataParts.ARTIST);
            String album = intent.getStringExtra(MetadataHelper.MetadataParts.ALBUM);
            String track = intent.getStringExtra(MetadataHelper.MetadataParts.TRACK);

            lastArtist = RTLHelper.ReorderRTLTextForPebble(artist, PebbleHelper.MAX_LARGE_BOLD_TEXT_PER_LINE);
            lastTrack = RTLHelper.ReorderRTLTextForPebble(track, PebbleHelper.MAX_SMALL_BOLD_TEXT_PER_LINE);
            lastAlbum = RTLHelper.ReorderRTLTextForPebble(album, PebbleHelper.MAX_LARGE_BOLD_TEXT_PER_LINE);

            PebbleHelper.SendMetadataToWatch(getApplicationContext(), lastArtist, lastAlbum, lastTrack);

            Intent localIntent = new Intent(LOCAL_INTENT);
            localIntent.putExtra(LOCAL_DATA, new String[]{artist, track, album});
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
        }
    };

    private BroadcastReceiver mPebbleConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Pebble connected!");
        }
    };

    private BroadcastReceiver mPebbleDisconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Pebble Disconnected!");
        }
    };

    private PebbleKit.PebbleAckReceiver mPebbleAckReceiver = new PebbleKit.PebbleAckReceiver(PebbleHelper.PEBBLE_APP_UUID) {
        @Override
        public void receiveAck(Context context, int transactionId) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(LOG_TAG, "Received ack from watch app!");
                }
            });
        }
    };

    private PebbleKit.PebbleNackReceiver mPebbleNackReceiver = new PebbleKit.PebbleNackReceiver(PebbleHelper.PEBBLE_APP_UUID) {
        @Override
        public void receiveNack(Context context, int transactionId) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(LOG_TAG, "Received nack from watch app!");
                }
            });
        }
    };

    private PebbleKit.PebbleDataReceiver mPebbleDataReceiver = new PebbleKit.PebbleDataReceiver(PebbleHelper.PEBBLE_APP_UUID) {
        @Override
        public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
            // Handle Android UI operations in a handler
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);

                    // If we don't have anything in the dictionary, stop.
                    if (!data.iterator().hasNext()) {
                        return;
                    }

                    if (data.contains(PebbleHelper.AppKeys.INIT)) {
                        Log.i(LOG_TAG, "Received init data from watch app.");
                        PebbleHelper.SendMetadataToWatch(getApplicationContext(), lastArtist, lastAlbum, lastTrack);
                    } else if (data.contains(PebbleHelper.AppKeys.PLAY_PAUSE)) {
                        Log.i(LOG_TAG, "watch app wants to " + (data.getInteger(PebbleHelper.AppKeys.PLAY_PAUSE) == 1 ? "Play!" : "Pause!"));
                        sendMediaKeyIntents(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                    } else if (data.contains(PebbleHelper.AppKeys.BACKWARD)) {
                        Log.i(LOG_TAG, "watch app wants to go to previous track!");
                        sendMediaKeyIntents(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                    } else if (data.contains(PebbleHelper.AppKeys.FORWARD)) {
                        Log.i(LOG_TAG, "watch app wants to go to next track!");
                        sendMediaKeyIntents(KeyEvent.KEYCODE_MEDIA_NEXT);
                    } else if (data.contains(PebbleHelper.AppKeys.VOLUME_DOWN)) {
                        Log.i(LOG_TAG, "watch app wants to decrease volume!");
                        audioManagerInstance.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                    } else if (data.contains(PebbleHelper.AppKeys.VOLUME_UP)) {
                        Log.i(LOG_TAG, "watch app wants to increase volume!");
                        audioManagerInstance.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                    } else {
                        Log.i(LOG_TAG, "Received unknown data from watch app!");
                    }
                }
            });
        }
    };

    private void sendMediaKeyIntents(int keyEventCode) {
        KeyEvent keDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode);
        KeyEvent keUp = new KeyEvent(KeyEvent.ACTION_UP, keyEventCode);

        // Use the new AudioManager API method on KitKat and greater, otherwise fallback to sending Intents.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            audioManagerInstance.dispatchMediaKeyEvent(keDown);
            audioManagerInstance.dispatchMediaKeyEvent(keUp);
        } else {
            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.putExtra(Intent.EXTRA_KEY_EVENT, keDown);
            sendOrderedBroadcast(i, null);

            i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.putExtra(Intent.EXTRA_KEY_EVENT, keUp);
            sendOrderedBroadcast(i, null);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public MetaChangedService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        audioManagerInstance = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        // Register our metadata change receiver and flag we're running already.
        registerReceiver(mMetaChangedReceiver, new IntentFilter(MetadataHelper.ANDROID_MUSIC_META_CHANGED_INTENT));

        // Register Pebble receivers.
        PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), mPebbleConnectedReceiver);
        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), mPebbleDisconnectedReceiver);
        PebbleKit.registerReceivedAckHandler(getApplicationContext(), mPebbleAckReceiver);
        PebbleKit.registerReceivedNackHandler(getApplicationContext(), mPebbleNackReceiver);
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), mPebbleDataReceiver);

        IsRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cleanup receivers and flag.
        unregisterReceiver(mMetaChangedReceiver);
        unregisterReceiver(mPebbleConnectedReceiver);
        unregisterReceiver(mPebbleDisconnectedReceiver);
        unregisterReceiver(mPebbleAckReceiver);
        unregisterReceiver(mPebbleNackReceiver);
        unregisterReceiver(mPebbleDataReceiver);

        IsRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We're not binding in this service.
        return null;
    }
}
