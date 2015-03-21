package com.ransagy.musicaforpebble;

import android.content.Context;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class PebbleHelper {

    public final static UUID PEBBLE_APP_UUID = UUID.fromString("71bfc6b0-3969-463d-857b-14ad6004224b");

    public class AppKeys {
        public static final int ARTIST = 0x0;
        public static final int TRACK = 0x1;
        public static final int ALBUM = 0x2;
        public static final int PLAYPAUSE = 0x3;
        public static final int INIT = 0x4;
        public static final int BACKWARD = 0x5;
        public static final int FORWARD = 0x6;
        public static final int VOLUME_DOWN = 0x7;
        public static final int VOLUME_UP = 0x8;
    }

    public static void SendMetadataToWatch(Context appContext, String artist, String album, String track) {
        boolean connected = PebbleKit.isWatchConnected(appContext);

        if (connected && artist != null && album != null && track != null) {
            PebbleDictionary data = new PebbleDictionary();

            data.addString(PebbleHelper.AppKeys.ARTIST, artist);
            data.addString(PebbleHelper.AppKeys.TRACK, track);
            data.addString(PebbleHelper.AppKeys.ALBUM, album);

            PebbleKit.sendDataToPebble(appContext, PebbleHelper.PEBBLE_APP_UUID, data);
        }
    }
}
