package com.ransagy.musicaforpebble;

import java.util.HashMap;
import java.util.Set;

public class MetadataHelper {

    private static HashMap<String, HashMap<MetadataPartsEnum, String>> metaMap;

    public enum MetadataPartsEnum {
        TRACK,
        ALBUM,
        ARTIST
    }

    static {
        metaMap = new HashMap<>();

        HashMap<MetadataPartsEnum,String> standardTags = new HashMap<>();
        standardTags.put(MetadataPartsEnum.ALBUM, "album");
        standardTags.put(MetadataPartsEnum.ARTIST, "artist");
        standardTags.put(MetadataPartsEnum.TRACK, "track");

        HashMap<MetadataPartsEnum,String> amazonTags = new HashMap<>();
        amazonTags.put(MetadataPartsEnum.ALBUM, "com.amazon.mp3.album");
        amazonTags.put(MetadataPartsEnum.ARTIST, "com.amazon.mp3.artist");
        amazonTags.put(MetadataPartsEnum.TRACK, "com.amazon.mp3.track");

        metaMap.put("com.android.music.metachanged", standardTags);
        metaMap.put("com.amazon.mp3.metachanged", amazonTags);
    }

    public static Set<String> GetMetaChangedActions() {
        return metaMap.keySet();
    }

    public static String GetMetadataPartName(String metaChangedAction, MetadataPartsEnum part) {
        if (metaMap == null || metaMap.size() == 0 || !metaMap.containsKey(metaChangedAction)) {
            return null;
        } else {
            return metaMap.get(metaChangedAction).get(part);
        }
    }
}
