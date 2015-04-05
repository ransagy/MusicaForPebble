package com.ransagy.musicaforpebble;

import android.content.Context;
import android.util.Pair;

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
        public static final int EXTRA_ARTIST = 0x9;
        public static final int EXTRA_TRACK = 0xA;
        public static final int EXTRA_ALBUM = 0xB;
    }

    public static void SendMetadataToWatch(Context appContext, Pair<String, String> artist, Pair<String, String> album, Pair<String, String> track) {
        boolean connected = PebbleKit.isWatchConnected(appContext);

        if (connected && artist != null && album != null && track != null) {
            PebbleDictionary data = new PebbleDictionary();

            data.addString(PebbleHelper.AppKeys.ARTIST, artist.first);
            data.addString(PebbleHelper.AppKeys.TRACK, track.first);
            data.addString(PebbleHelper.AppKeys.ALBUM, album.first);

            if (artist.second.length() > 0) {
                data.addString(PebbleHelper.AppKeys.EXTRA_ARTIST, artist.second);
            }

            if (track.second.length() > 0) {
                data.addString(PebbleHelper.AppKeys.EXTRA_TRACK, track.second);
            }

            if (album.second.length() > 0) {
                data.addString(PebbleHelper.AppKeys.EXTRA_ALBUM, album.second);
            }

            PebbleKit.sendDataToPebble(appContext, PebbleHelper.PEBBLE_APP_UUID, data);
        }
    }
}
