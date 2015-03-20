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

public class MetaChangedService extends Service {

    public static boolean IsRunning = false;

    private final String LOG_TAG = this.getClass().getSimpleName();
    private Handler uiHandler = new Handler();

    private BroadcastReceiver mMetaChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract metadata from intent.
            String artist = intent.getStringExtra(MetaHelper.MetadataParts.ARTIST);
            String album = intent.getStringExtra(MetaHelper.MetadataParts.ALBUM);
            String track = intent.getStringExtra(MetaHelper.MetadataParts.TRACK);

            boolean connected = PebbleKit.isWatchConnected(getApplicationContext());

            if (connected && artist != null && album != null && track != null) {
                PebbleDictionary data = new PebbleDictionary();

                data.addString(PebbleHelper.AppKeys.ARTIST, RTLHelper.ReorderRTLTextForPebble(artist));
                data.addString(PebbleHelper.AppKeys.TRACK, RTLHelper.ReorderRTLTextForPebble(track));
                data.addString(PebbleHelper.AppKeys.ALBUM, RTLHelper.ReorderRTLTextForPebble(album));

                PebbleKit.sendDataToPebble(getApplicationContext(), PebbleHelper.PEBBLE_APP_UUID, data);
            }
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
