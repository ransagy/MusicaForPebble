package com.ransagy.musicaforpebble;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MetaChangedService extends Service {

    public static boolean IsRunning = false;

    private AudioManager audioManagerInstance;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private Handler uiHandler = new Handler();

    private Pair<String,String> lastArtist = new Pair<>("","");
    private Pair<String,String> lastTrack = new Pair<>("","");
    private Pair<String,String> lastAlbum = new Pair<>("","");

    private BroadcastReceiver mMetaChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract metadata from intent.
            String artist = intent.getStringExtra(MetaHelper.MetadataParts.ARTIST);
            String album = intent.getStringExtra(MetaHelper.MetadataParts.ALBUM);
            String track = intent.getStringExtra(MetaHelper.MetadataParts.TRACK);

            lastArtist = RTLHelper.ReorderRTLTextForPebble(artist, 13);
            lastTrack = RTLHelper.ReorderRTLTextForPebble(track, 10);
            lastAlbum = RTLHelper.ReorderRTLTextForPebble(album, 13);

            PebbleHelper.SendMetadataToWatch(getApplicationContext(), lastArtist, lastAlbum, lastTrack);
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
            Log.i(LOG_TAG, "Received ack for transaction " + transactionId);
        }
    };

    private PebbleKit.PebbleNackReceiver mPebbleNackReceiver = new PebbleKit.PebbleNackReceiver(PebbleHelper.PEBBLE_APP_UUID) {
        @Override
        public void receiveNack(Context context, int transactionId) {
            Log.i(LOG_TAG, "Received nack for transaction " + transactionId);
        }
    };

    private PebbleKit.PebbleDataReceiver mPebbleDataReceiver = new PebbleKit.PebbleDataReceiver(PebbleHelper.PEBBLE_APP_UUID) {
        @Override
        public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {

            if (data.contains(PebbleHelper.AppKeys.INIT)) {
                Log.i(LOG_TAG, "Received init data from watchapp.");
                PebbleHelper.SendMetadataToWatch(getApplicationContext(), lastArtist, lastAlbum, lastTrack);
            } else if (data.contains(PebbleHelper.AppKeys.PLAYPAUSE)) {
                Log.i(LOG_TAG, "watchapp wants to " + (data.getInteger(PebbleHelper.AppKeys.PLAYPAUSE) == 1 ? "Play!" : "Pause!"));
                sendMediaKeyIntents(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            } else if (data.contains(PebbleHelper.AppKeys.BACKWARD)) {
                Log.i(LOG_TAG, "watchapp wants to go to previous track!");
                sendMediaKeyIntents(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            } else if (data.contains(PebbleHelper.AppKeys.FORWARD)) {
                Log.i(LOG_TAG, "watchapp wants to go to next track!");
                sendMediaKeyIntents(KeyEvent.KEYCODE_MEDIA_NEXT);
            } else if (data.contains(PebbleHelper.AppKeys.VOLUME_DOWN)) {
                Log.i(LOG_TAG, "watchapp wants to decrease volume!");
                audioManagerInstance.adjustVolume(AudioManager.ADJUST_LOWER, 0);
            } else if (data.contains(PebbleHelper.AppKeys.VOLUME_UP)) {
                Log.i(LOG_TAG, "watchapp wants to increase volume!");
                audioManagerInstance.adjustVolume(AudioManager.ADJUST_RAISE, 0);
            } else {
                Log.i(LOG_TAG, "Received unknown data from watchapp!");
            }

            // Handle Android UI operations in a handler
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                        /* Update your UI here. */
                }
            });

            PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
        }
    };

    private void sendMediaKeyIntents(int keyEventCode) {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        sendOrderedBroadcast(i, null);

        i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
        sendOrderedBroadcast(i, null);
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
        registerReceiver(mMetaChangedReceiver, new IntentFilter(MetaHelper.ANDROID_MUSIC_METACHANGED_INTENT));

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
